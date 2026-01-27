package com.ssafy.closetory.homeActivity.registrationCloth

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import kotlin.math.log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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

    // 최종 선택된 "통일된" Uri (항상 fileprovider + closetory_*.png)
    private var selectedImageUri: Uri? = null

    private val registrationClothViewModel: RegistrationClothViewModel by viewModels()

    // 카메라 Uri에 원본 저장 (selectedImageUri에 미리 넣어둔 Uri로 저장됨)
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (!success) return@registerForActivityResult
            val uri = selectedImageUri ?: return@registerForActivityResult
            onPhotoSelected(uri) // 카메라도 공통 처리
        }

    // 갤러리 Uri 받기
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@registerForActivityResult

            // 갤러리 Uri를 cache/images에 "closetory_*.png"로 저장해서 통일 Uri로 변환
            val normalized = copyUriAsPngToCache(uri)
            if (normalized == null) {
                showToast("이미지 처리에 실패했습니다.")
                return@registerForActivityResult
            }

            onPhotoSelected(normalized)
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

        // 처음엔 사진 없음 → placeholder 보여야 함
        updatePhotoPlaceholder(isPhotoSelected = false)

        binding.imbtnRegistrationCloth.setOnClickListener {
            AlertDialog.Builder(homeActivity)
                .setTitle("사진 가져오기")
                .setItems(arrayOf("카메라 촬영", "갤러리 선택")) { _, which ->
                    when (which) {
                        0 -> openCamera()
                        1 -> openGalleryPicker()
                    }
                }
                .show()
        }

        // 옷 등록
        binding.btnRegistrationCloth.setOnClickListener {
            val photoUri = selectedImageUri
            val selectedTags = TagOptions.getSelectedTag(tagsSection)
            val selectedClothType = ClothTypeOptions.getClothType(clothTypeSection)
            val selectedSeasons = SeasonOptions.getSelectedSeason(seasonSection)
            val selectedColor = colorAdapter.getSelectedColor()

            when {
                photoUri == null -> {
                    showToast("사진을 등록해주세요.")
                    return@setOnClickListener
                }

                selectedTags.isEmpty() -> {
                    showToast("태그를 1개 이상 선택해주세요.")
                    return@setOnClickListener
                }

                selectedClothType == null -> {
                    showToast("의류 종류를 선택해주세요.")
                    return@setOnClickListener
                }

                selectedSeasons.isEmpty() -> {
                    showToast("계절을 1개 이상 선택해주세요.")
                    return@setOnClickListener
                }

                selectedColor.isNullOrBlank() -> {
                    showToast("색상을 선택해주세요.")
                    return@setOnClickListener
                }
            }

            Log.d(
                TAG,
                "photo : $photoUri selectedTags : $selectedTags selectedClothType : $selectedClothType selectedSeasons : $selectedSeasons, selectedColor $selectedColor"
            )

            registrationClothViewModel.registrationCloth(
                photoUrl = photoUri!!,
                tags = selectedTags,
                clothesTypes = selectedClothType!!,
                seasons = selectedSeasons,
                color = selectedColor!!
            )
        }

        registerObserve()
    }

    // 사진 선택 공통 처리 (항상 "통일된 Uri"만 들어오게 설계)
    private fun onPhotoSelected(uri: Uri) {
        selectedImageUri = uri
        binding.imbtnRegistrationCloth.setImageURI(uri)
        updatePhotoPlaceholder(isPhotoSelected = true)

        // 필요하면 여기서 multipart 변환까지 해두면 됨
        val multipart: MultipartBody.Part = uriToMultipart(uri)
        Log.d(TAG, "normalized uri = $uri, multipart size prepared")
        // 서버 전송은 ViewModel에서 처리

        registrationClothViewModel.removeImageBackground(multipart)
    }

    private fun updatePhotoPlaceholder(isPhotoSelected: Boolean) {
        binding.tvPhotoPlaceholder.visibility = if (isPhotoSelected) View.GONE else View.VISIBLE
    }

    private fun setupOptionSection() {
        TagOptions.render(tagsSection, homeActivity)
        SeasonOptions.render(seasonSection, homeActivity)
        ClothTypeOptions.render(clothTypeSection, homeActivity)
        colorAdapter = ColorOptions.setup(colorSection)
    }

    private fun openGalleryPicker() {
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun openCamera() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (cameraPermissionChecker.checkPermission(homeActivity, permissions)) {
            launchCameraToUri()
            return
        }
        cameraPermissionChecker.setOnGrantedListener { launchCameraToUri() }
        cameraPermissionChecker.requestPermissions(permissions)
    }

    private fun launchCameraToUri() {
        // 카메라는 애초에 cache/images/closetory_*.png Uri를 만들어서 저장하게 함 → 이미 통일됨
        selectedImageUri = createImageUri()
        selectedImageUri?.let { takePicture.launch(it) }
    }

    // 통일된 파일 Uri 생성
    private fun createImageUri(): Uri? = try {
        val dir = File(requireContext().cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "closetory_${System.currentTimeMillis()}.png")
        FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
    } catch (_: Exception) {
        null
    }

    private fun copyUriAsPngToCache(sourceUri: Uri): Uri? {
        return try {
            val cr = requireContext().contentResolver

            val bitmap = cr.openInputStream(sourceUri)?.use { BitmapFactory.decodeStream(it) }
                ?: return null

            val dir = File(requireContext().cacheDir, "images").apply { mkdirs() }
            val outFile = File(dir, "closetory_${System.currentTimeMillis()}.png")

            outFile.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                outFile
            )
        } catch (_: Exception) {
            null
        }
    }

    // Uri -> Binary(Multipart) 변환
    private fun uriToMultipart(uri: Uri): MultipartBody.Part {
        val cr = requireContext().contentResolver
        val bytes = cr.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("이미지 스트림을 열 수 없습니다: $uri")

        val requestBody = bytes.toRequestBody("image/png".toMediaTypeOrNull())
        val fileName = "closetory_${System.currentTimeMillis()}.png"

        return MultipartBody.Part.createFormData("photo", fileName, requestBody)
    }

    @SuppressLint("CheckResult")
    private fun registerObserve() {
        registrationClothViewModel.maskedImage.observe(viewLifecycleOwner) { url ->

            Log.d(TAG, "서버로 부터 전달 받은 url : $url")
            Glide.with(binding.imbtnRegistrationCloth)
                .load(url)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
        }
    }
}
