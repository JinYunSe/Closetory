package com.ssafy.closetory.homeActivity.registrationCloth

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentRegistrationClothBinding
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.util.ClothTypeOptions
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.PermissionChecker
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions
import java.io.File

/**
 * On-device AI(세그멘테이션/배경제거) 없이,
 * 사진 선택 즉시 imbtnRegistrationCloth에 반영하는 버전
 */
class RegistrationClothFragment :
    BaseFragment<FragmentRegistrationClothBinding>(
        FragmentRegistrationClothBinding::bind,
        R.layout.fragment_registration_cloth
    ) {

    private lateinit var homeActivity: HomeActivity
    private val cameraPermissionChecker = PermissionChecker()

    private lateinit var colorAdapter: ColorOptions.ColorAdapter
    private lateinit var tagsSection: View
    private lateinit var seasonSection: View
    private lateinit var clothTypeSection: View
    private lateinit var colorSection: View

    private var selectedImageUri: Uri? = null

    private val registrationClothViewModel: RegistrationClothViewModel by viewModels()

    // 카메라 촬영 결과를 미리 만든 Uri에 저장
    private val captureToUriLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (!success) return@registerForActivityResult
            selectedImageUri?.let { onImageSelected(it) }
        }

    // 갤러리(포토피커)에서 이미지 Uri 선택
    private val photoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { onImageSelected(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraPermissionChecker.init(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireActivity() as HomeActivity

        tagsSection = view.findViewById(R.id.section_tags)
        seasonSection = view.findViewById(R.id.section_season)
        clothTypeSection = view.findViewById(R.id.section_cloth_type)
        colorSection = view.findViewById(R.id.section_color)

        setupOptionSection()

        // ✅ 사진 선택(카메라/갤러리)만 다이얼로그로 받고, 선택되면 즉시 버튼에 반영
        binding.imbtnRegistrationCloth.setOnClickListener { showPickDialog() }

        binding.btnRegistrationCloth.setOnClickListener {
            val photoUri = selectedImageUri
            val selectedTags = TagOptions.getSelectedTag(tagsSection)
            val selectedClothType = ClothTypeOptions.getClothType(clothTypeSection)
            val selectedSeasons = SeasonOptions.getSelectedSeason(seasonSection)
            val selectedColor = colorAdapter.getSelectedColor()

            if (photoUri == null ||
                selectedTags.isEmpty() ||
                selectedClothType == null ||
                selectedSeasons.isEmpty() ||
                selectedColor.isNullOrBlank()
            ) {
                showToast("모든 항목을 입력해주세요.")
                return@setOnClickListener
            }

            registrationClothViewModel.registrationCloth(
                photoUri,
                selectedTags,
                selectedClothType,
                selectedSeasons,
                selectedColor
            )
        }
    }

    /**
     * 카메라/갤러리 선택 다이얼로그 (이미지 미리보기 다이얼로그는 없음)
     */
    private fun showPickDialog() {
        AlertDialog.Builder(homeActivity)
            .setTitle("사진 가져오기")
            .setItems(arrayOf("카메라 촬영", "갤러리 선택")) { _, which ->
                when (which) {
                    0 -> ensureCameraPermissionThenLaunch()
                    1 -> launchPhotoPicker()
                }
            }
            .show()
    }

    private fun onImageSelected(uri: Uri) {
        selectedImageUri = uri
        binding.imbtnRegistrationCloth.setImageURI(uri)
    }

    private fun setupOptionSection() {
        TagOptions.render(tagsSection, homeActivity)
        SeasonOptions.render(seasonSection, homeActivity)
        ClothTypeOptions.render(clothTypeSection, homeActivity)
        colorAdapter = ColorOptions.setup(colorSection)
    }

    private fun launchPhotoPicker() {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun ensureCameraPermissionThenLaunch() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (cameraPermissionChecker.checkPermission(homeActivity, permissions)) {
            launchCameraCaptureToUri()
            return
        }
        cameraPermissionChecker.setOnGrantedListener { launchCameraCaptureToUri() }
        cameraPermissionChecker.requestPermissions(permissions)
    }

    private fun launchCameraCaptureToUri() {
        selectedImageUri = createImageUri()
        selectedImageUri?.let { captureToUriLauncher.launch(it) }
    }

    private fun createImageUri(): Uri? = try {
        val dir = File(requireContext().cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "closetory_${System.currentTimeMillis()}.png")
        FileProvider.getUriForFile(
            requireContext(),
            "${homeActivity.packageName}.fileprovider",
            file
        )
    } catch (_: Exception) {
        null
    }

    private fun uriToBitmap(uri: Uri): Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.isMutableRequired = true
        }
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
    }

    private fun Bitmap.toArgb8888Mutable(): Bitmap {
        val base = if (config == Bitmap.Config.ARGB_8888) this else copy(Bitmap.Config.ARGB_8888, false)
        return base.copy(Bitmap.Config.ARGB_8888, true)
    }

    private fun Bitmap.downscaleIfNeeded(maxSide: Int): Bitmap {
        val m = maxOf(width, height)
        if (m <= maxSide) return this
        val scale = maxSide.toFloat() / m.toFloat()
        return Bitmap.createScaledBitmap(
            this,
            (width * scale).toInt(),
            (height * scale).toInt(),
            true
        )
    }
}
