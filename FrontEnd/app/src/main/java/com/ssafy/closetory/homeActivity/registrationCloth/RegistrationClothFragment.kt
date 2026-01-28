// RegistrationClothFragment.kt
package com.ssafy.closetory.homeActivity.registrationCloth

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentRegistrationClothBinding
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.util.ClothTypeOptions
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.PermissionChecker
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions
import com.ssafy.closetory.util.image.ImageMultipartUtil
import java.io.File

private const val TAG = "RegistrationClothFragme_싸피"

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
    private val viewModel: RegistrationClothViewModel by viewModels()

    // 배경 제거 요청 중이면 사진 재선택/재촬영을 막는다
    private var isMaskingInProgress: Boolean = false

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) selectedImageUri?.let { onPhotoSelected(it) }
        }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { onPhotoSelected(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraPermissionChecker.init(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireContext() as HomeActivity

        tagsSection = view.findViewById(R.id.section_tags)
        seasonSection = view.findViewById(R.id.section_season)
        clothTypeSection = view.findViewById(R.id.section_cloth_type)
        colorSection = view.findViewById(R.id.section_color)

        setupOptionSection()

        showPhotoPlaceholder("사진 등록")
        binding.btnRegistrationCloth.isEnabled = false

        binding.imbtnRegistrationCloth.setOnClickListener {
            if (isMaskingInProgress) {
                showToast("잠시 후 다시 시도해주세요.")
                return@setOnClickListener
            }

            AlertDialog.Builder(homeActivity)
                .setItems(arrayOf("카메라 촬영", "갤러리 선택")) { _, which ->
                    if (which == 0) openCamera() else openGalleryPicker()
                }
                .show()
        }

        binding.btnRegistrationCloth.setOnClickListener {
            val maskedUrl = viewModel.maskedImage.value ?: return@setOnClickListener

            viewModel.registrationCloth(
                photoUrl = maskedUrl,
                tags = TagOptions.getSelectedTag(tagsSection),
                clothesTypes = ClothTypeOptions.getClothType(clothTypeSection) ?: return@setOnClickListener,
                seasons = SeasonOptions.getSelectedSeason(seasonSection),
                color = colorAdapter.getSelectedColor() ?: return@setOnClickListener
            )
        }

        registerObserve()
    }

    private fun onPhotoSelected(uri: Uri) {
        // 이전 표시 이미지 제거
        // Gilde과 imageButton 둘 다 제거
        Glide.with(binding.imbtnRegistrationCloth).clear(binding.imbtnRegistrationCloth)
        binding.imbtnRegistrationCloth.setImageDrawable(null)

        // 배경 제거 중 상태로 전환: 사진 재선택, 재촬영 막기
        isMaskingInProgress = true

        binding.btnRegistrationCloth.isEnabled = false
        viewModel.clearMaskedUrl()
        showPhotoPlaceholder("배경 제거 중...")

        val multipart = ImageMultipartUtil.uriToCompressedMultipart(
            context = requireContext(),
            uri = uri,
            partName = "clothesPhotoUrl",
            maxBytes = 600 * 1024,
            maxDimension = 1280
        )

        Log.d(TAG, "send masking image")
        viewModel.removeImageBackground(multipart)
    }

    private fun setupOptionSection() {
        TagOptions.render(tagsSection, homeActivity)
        SeasonOptions.render(seasonSection, homeActivity)
        ClothTypeOptions.render(clothTypeSection, homeActivity)
        colorAdapter = ColorOptions.setup(colorSection)
    }

    private fun showPhotoPlaceholder(text: String) {
        binding.tvPhotoPlaceholder.text = text
        binding.tvPhotoPlaceholder.visibility = View.VISIBLE
    }

    private fun hidePhotoPlaceholder() {
        binding.tvPhotoPlaceholder.visibility = View.GONE
    }

    private fun openGalleryPicker() {
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun openCamera() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (cameraPermissionChecker.checkPermission(homeActivity, permissions)) {
            launchCameraToUri()
        } else {
            cameraPermissionChecker.setOnGrantedListener { launchCameraToUri() }
            cameraPermissionChecker.requestPermissions(permissions)
        }
    }

    private fun launchCameraToUri() {
        selectedImageUri = createImageUri()
        selectedImageUri?.let { takePicture.launch(it) }
    }

    private fun createImageUri(): Uri? = try {
        val dir = File(requireContext().cacheDir, "images").apply { mkdirs() }
        val file =
            File(
                dir,
                "closetory_${ApplicationClass.sharedPreferences.getUserId(
                    ApplicationClass.USERID
                )}_${System.currentTimeMillis()}.jpg"
            )
        FileProvider.getUriForFile(
            homeActivity,
            "${requireContext().packageName}.fileprovider",
            file
        )
    } catch (_: Exception) {
        null
    }

    @SuppressLint("CheckResult")
    private fun registerObserve() {
        viewModel.maskedImage.observe(viewLifecycleOwner) { url ->
            if (url.isNullOrBlank()) return@observe

            Glide.with(binding.imbtnRegistrationCloth)
                .load(url)
                .into(binding.imbtnRegistrationCloth)

            // 서버가 마스킹 이미지 URL을 준 시점: 다시 사진 선택/촬영 허용
            isMaskingInProgress = false

            binding.btnRegistrationCloth.isEnabled = true
            hidePhotoPlaceholder()
        }
    }
}
