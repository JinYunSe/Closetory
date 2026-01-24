package com.ssafy.closetory.homeActivity.addClose

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentAddClothBinding
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.util.ClothTypeOptions
import com.ssafy.closetory.util.ColorOptions
import com.ssafy.closetory.util.PermissionChecker
import com.ssafy.closetory.util.SeasonOptions
import com.ssafy.closetory.util.TagOptions
import java.io.File

class AddClothFragment :
    BaseFragment<FragmentAddClothBinding>(FragmentAddClothBinding::bind, R.layout.fragment_add_cloth) {

    private lateinit var homeActivity: HomeActivity

    // 카메라 권한 요청이 들어올 경우 초기화해서 사용하도록 지정
    private val cameraPermissionChecker = PermissionChecker()

    private lateinit var colorAdapter: ColorOptions.ColorAdapter

    private lateinit var tagsSection: View

    private lateinit var seasonSection: View

    private lateinit var clothTypeSection: View

    private lateinit var colorSection: View

    // 카메라 원본 저장용 Uri
    private var capturedImageUri: Uri? = null

    // 권한 요청 런처
    private var onCameraPermissionGranted: (() -> Unit)? = null

    // 카메라 런처
    private val captureToUriLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (!success) return@registerForActivityResult
            val uri = capturedImageUri ?: return@registerForActivityResult
            binding.imbtnRegistrationCloth.setImageURI(uri)
            showPhotoPlaceholder(false)
        }

    // 갤러리 런처
    private val photoPickerLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri ?: return@registerForActivityResult
        binding.imbtnRegistrationCloth.setImageURI(uri)
        showPhotoPlaceholder(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraPermissionChecker.init(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeActivity = requireActivity() as HomeActivity

        // 처음에는 사진이 없어서 문구 보이게 만들기
        showPhotoPlaceholder(true)

        // 다른 XML 레이어 파일 가져오기
        tagsSection = view.findViewById<View>(R.id.section_tags)
        seasonSection = view.findViewById<View>(R.id.section_season)
        clothTypeSection = view.findViewById<View>(R.id.section_cloth_type)
        colorSection = view.findViewById<View>(R.id.section_color)

        setupOptionSection()

        binding.imbtnRegistrationCloth.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(homeActivity)
                .setTitle("사진 가져오기")
                .setItems(arrayOf("카메라 촬영", "갤러리 선택")) { _, which ->
                    when (which) {
                        0 -> ensureCameraPermissionThenLaunch()
                        1 -> launchPhotoPicker()
                    }
                }.show()
        }
    }

    // 태그, 계절, 옷 옵션, 색상 정보 UI애 반영하기
    fun setupOptionSection() {
        TagOptions.render(tagsSection, homeActivity)
        SeasonOptions.render(seasonSection, homeActivity)
        ClothTypeOptions.render(clothTypeSection, homeActivity)
        colorAdapter = ColorOptions.setup(colorSection)
    }

    // 갤러리 실행
    private fun launchPhotoPicker() {
        // 카메라에서 이미지 1장 가져올 것이라 명시
        photoPickerLauncher.launch(
            androidx.activity.result.PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    // 카메라 권한 확인 후 촬영 실행
    private fun ensureCameraPermissionThenLaunch() {
        // 카메라 권한 체크
        val permissions = arrayOf(android.Manifest.permission.CAMERA)

        // 권한 있을 경우 카메라 실행
        if (cameraPermissionChecker.checkPermission(homeActivity, permissions)) {
            launchCameraCaptureToUri()
            return
        }

        // 권한 요청창 띄우기
        cameraPermissionChecker.setOnGrantedListener {
            launchCameraCaptureToUri()
        }

        // 권한 요청하기
        cameraPermissionChecker.requestPermissions(permissions)
    }

    // 카메라에 사진 원본 저장
    private fun launchCameraCaptureToUri() {
        capturedImageUri = createImageUri()
        val uri = capturedImageUri ?: return
        captureToUriLauncher.launch(uri)
    }

    // 카메라 촬영 결과를 저장할 Uri 생성
    private fun createImageUri(): Uri? = try {
        // 이미지 파일로 지정
        val imagesDir = File(requireContext().cacheDir, "images").apply { mkdirs() }
        // 파일명 등록
        val imageFile = File(imagesDir, "closetory_${System.currentTimeMillis()}.png")

        // FileProvider 결과물을 Uri로 변환
        FileProvider.getUriForFile(
            requireContext(),
            "${homeActivity.packageName}.fileprovider",
            imageFile
        )
    } catch (_: Exception) {
        null
    }

    private fun showPhotoPlaceholder(show: Boolean) {
        binding.tvPhotoPlaceholder.visibility = if (show) View.VISIBLE else View.GONE
    }
}
