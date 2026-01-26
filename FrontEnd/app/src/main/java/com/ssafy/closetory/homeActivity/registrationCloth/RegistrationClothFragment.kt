package com.ssafy.closetory.homeActivity.registrationCloth

import android.Manifest
import android.net.Uri
import android.os.Bundle
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody

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

    // 카메라 Uri에 원본 저장
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (!success) return@registerForActivityResult
            val uri = selectedImageUri ?: return@registerForActivityResult
            val binary = uriToMultipart(uri)
            registrationClothViewModel.removeImageBackground(binary)
        }

    // 갤러리 Uri 받기
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@registerForActivityResult
            val binary = uriToMultipart(uri)
            registrationClothViewModel.removeImageBackground(binary)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 카메라 권한
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

        // "사진 등록 텍스트 보이게 설정
        updatePhotoPlaceholder(false)

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

            registrationClothViewModel.registrationCloth(
                photoUri!!,
                selectedTags,
                selectedClothType!!,
                selectedSeasons,
                selectedColor!!
            )
        }
    }

    // 등록된 사진 공통 처리
    private fun onPhotoSelected(uri: Uri) {
        selectedImageUri = uri
        binding.imbtnRegistrationCloth.setImageURI(uri)
        updatePhotoPlaceholder(true)

        // TODO: Uri -> Binary(Multipart)로 변환까지만
        val multipart: MultipartBody.Part = uriToMultipart(uri)
        // TODO: 서버 전송은 이후에 구현
    }

    // "사진 등록" 보이게, 안 보이게 설정
    private fun updatePhotoPlaceholder(isPhotoSelected: Boolean) {
        binding.tvPhotoPlaceholder.visibility = if (isPhotoSelected) View.GONE else View.VISIBLE
    }

    // 태그, 계절, 옷 종류, 색상 요소 UI 반영
    private fun setupOptionSection() {
        TagOptions.render(tagsSection, homeActivity)
        SeasonOptions.render(seasonSection, homeActivity)
        ClothTypeOptions.render(clothTypeSection, homeActivity)
        colorAdapter = ColorOptions.setup(colorSection)
    }

    // 갤러리 실행
    private fun openGalleryPicker() {
        pickImage.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    // 카메라 실행
    private fun openCamera() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (cameraPermissionChecker.checkPermission(homeActivity, permissions)) {
            launchCameraToUri()
            return
        }
        cameraPermissionChecker.setOnGrantedListener { launchCameraToUri() }
        cameraPermissionChecker.requestPermissions(permissions)
    }

    // 통일 된 양식으로
    private fun launchCameraToUri() {
        selectedImageUri = createImageUri()
        selectedImageUri?.let { takePicture.launch(it) }
    }

    // 갤러리, 카메라에서 가져온 사진 양식 통일
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

    // Uri -> Binary(Multipart) 변환
    private fun uriToMultipart(uri: Uri): MultipartBody.Part {
        val cr = requireContext().contentResolver
        val bytes = cr.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("이미지 스트림을 열 수 없습니다: $uri")

        val mime = cr.getType(uri) ?: "image/*"
        val mediaType = mime.toMediaTypeOrNull() ?: "image/*".toMediaTypeOrNull()!!

        val requestBody = okhttp3.RequestBody.create(mediaType, bytes)

        val fileName = "upload_${System.currentTimeMillis()}.png"
        return MultipartBody.Part.createFormData("image", fileName, requestBody)
    }
}
