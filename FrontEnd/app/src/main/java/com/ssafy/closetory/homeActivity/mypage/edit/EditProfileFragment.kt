package com.ssafy.closetory.homeActivity.mypage.edit

import android.Manifest
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentEditProfileBinding
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.dto.EditProfileUpdateData
import com.ssafy.closetory.util.PermissionChecker
import com.ssafy.closetory.util.image.ImageMultipartUtil
import java.io.File
import kotlinx.coroutines.launch

private const val TAG = "EditProfileFragment_싸피"

class EditProfileFragment :
    BaseFragment<FragmentEditProfileBinding>(
        FragmentEditProfileBinding::bind,
        R.layout.fragment_edit_profile
    ) {

    // ViewModel 참조임
    private val viewModel: EditProfileViewModel by viewModels()

    // 카메라 권한 체크 유틸임
    private val cameraPermissionChecker = PermissionChecker()

    // 성별 상태 저장임
    private var gender: String? = null

    // 서버 사진 URL 저장임
    private var profilePhotoUrl: String? = null
    private var bodyPhotoUrl: String? = null

    // 선택된 프로필 사진 Uri 저장임
    private var selectedProfileUri: Uri? = null

    // 선택된 바디 사진 Uri 저장임
    private var selectedBodyUri: Uri? = null

    // 사진 선택 타겟 구분 enum임
    private enum class PhotoTarget { PROFILE, BODY }

    // 현재 사진 선택 타겟 저장임
    private var currentTarget: PhotoTarget? = null

    // 카메라 촬영 결과 처리임
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (!success) return@registerForActivityResult

            when (currentTarget) {
                PhotoTarget.PROFILE -> selectedProfileUri?.let { onProfileSelected(it) }
                PhotoTarget.BODY -> selectedBodyUri?.let { onBodySelected(it) }
                else -> Unit
            }
        }

    // 갤러리 선택 결과 처리임
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@registerForActivityResult

            when (currentTarget) {
                PhotoTarget.PROFILE -> {
                    selectedProfileUri = uri
                    onProfileSelected(uri)
                }

                PhotoTarget.BODY -> {
                    selectedBodyUri = uri
                    onBodySelected(uri)
                }

                else -> Unit
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 권한 체크 유틸 초기화임
        cameraPermissionChecker.init(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 사진 클릭 이벤트 연결임
        setupPhotoClick()

        // 버튼 클릭 이벤트 연결임
        clickListeners()

        // 성별 버튼 이벤트 연결임
        setupGenderButtons()

        // ViewModel Flow 관찰 연결임
        observeViewModel()

        // 회원정보 조회 요청 시작임
        loadUserProfile()
    }

    // 회원정보 조회 요청 함수임
    private fun loadUserProfile() {
        val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID) ?: return
        viewModel.loadUserProfile(userId)
    }

    // ViewModel 결과 관찰 처리임
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userProfile.collect { user ->
                bindUserProfile(user)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.message.collect { message ->
                if (!message.isNullOrBlank()) showToast(message)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateResult.collect { result ->
                if (result != null) findNavController().popBackStack()
            }
        }
    }

    // 회원정보 UI 바인딩 처리임
    private fun bindUserProfile(user: EditProfileInfoResponse) {
        binding.etNickname.setText(user.nickname)
        binding.etHeight.setText(user.height?.toString().orEmpty())
        binding.etWeight.setText(user.weight?.toString().orEmpty())
        binding.switchAlarm.isChecked = user.alarmEnabled

        gender = user.gender
        selectGender()

        profilePhotoUrl = user.profilePhotoUrl
        bodyPhotoUrl = user.bodyPhotoUrl

        if (user.profilePhotoUrl.isNullOrBlank()) {
            binding.imgProfile.setImageResource(R.drawable.ic_profile_default)
        } else {
            Glide.with(this)
                .load(user.profilePhotoUrl)
                .placeholder(R.drawable.ic_profile_default)
                .error(R.drawable.ic_profile_default)
                .into(binding.imgProfile)
        }

        if (user.bodyPhotoUrl.isNullOrBlank()) {
            binding.imgBody.setImageResource(R.drawable.ic_body_default)
        } else {
            Glide.with(this)
                .load(user.bodyPhotoUrl)
                .placeholder(R.drawable.ic_body_default)
                .error(R.drawable.ic_body_default)
                .into(binding.imgBody)
        }
    }

    // 사진 클릭 이벤트 세팅임
    private fun setupPhotoClick() {
        binding.layoutProfileImage.setOnClickListener {
            currentTarget = PhotoTarget.PROFILE
            showPhotoSourceDialog()
        }

        binding.layoutBodyImage.setOnClickListener {
            currentTarget = PhotoTarget.BODY
            showPhotoSourceDialog()
        }
    }

    // 사진 소스 선택 다이얼로그 표시임
    private fun showPhotoSourceDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("사진 가져오기")
            .setItems(arrayOf("카메라 촬영", "갤러리 선택")) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGalleryPicker()
                }
            }
            .show()
    }

    // 갤러리 포토피커 실행임
    private fun openGalleryPicker() {
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // 카메라 권한 확인 후 촬영 실행임
    private fun openCamera() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (cameraPermissionChecker.checkPermission(requireContext(), permissions)) {
            launchCameraToUri()
            return
        }
        cameraPermissionChecker.setOnGrantedListener { launchCameraToUri() }
        cameraPermissionChecker.requestPermissions(permissions)
    }

    // 카메라 저장 Uri 생성 후 TakePicture 실행임
    private fun launchCameraToUri() {
        val uri = createImageUri() ?: return
        when (currentTarget) {
            PhotoTarget.PROFILE -> selectedProfileUri = uri
            PhotoTarget.BODY -> selectedBodyUri = uri
            else -> return
        }
        takePicture.launch(uri)
    }

    // FileProvider 기반 임시 Uri 생성임
    private fun createImageUri(): Uri? = try {
        val dir = File(requireContext().cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "user_${System.currentTimeMillis()}.png")
        FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
    } catch (_: Exception) {
        null
    }

    // 프로필 사진 미리보기 반영 처리임
    private fun onProfileSelected(uri: Uri) {
        binding.imgProfile.setImageURI(uri)
    }

    // 바디 사진 미리보기 반영 처리임
    private fun onBodySelected(uri: Uri) {
        binding.imgBody.setImageURI(uri)
    }

    // 버튼 클릭 이벤트 및 입력 검증 처리임
    private fun clickListeners() {
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID, -1) ?: -1
            if (userId == -1) {
                showToast("로그인이 필요합니다.")
                return@setOnClickListener
            }

            val nickname = binding.etNickname.text.toString().trim()
            val heightText = binding.etHeight.text.toString().trim()
            val weightText = binding.etWeight.text.toString().trim()

            if (nickname.isBlank()) {
                showToast("닉네임을 입력해주세요.")
                return@setOnClickListener
            }

            if (nickname.length > 10) {
                showToast("닉네임은 10자 이하로 입력해주세요.")
                return@setOnClickListener
            }

            if (heightText.isBlank()) {
                showToast("키를 입력해주세요.")
                return@setOnClickListener
            }

            if (weightText.isBlank()) {
                showToast("몸무게를 입력해주세요.")
                return@setOnClickListener
            }

            val height = heightText.toShortOrNull()
            val weight = weightText.toShortOrNull()

            if (height == null) {
                showToast("키는 숫자로 입력해주세요.")
                return@setOnClickListener
            }

            if (weight == null) {
                showToast("몸무게는 숫자로 입력해주세요.")
                return@setOnClickListener
            }
            if (height < 100 || height > 250) {
                showToast("키는 100~250 범위로 입력해주세요.")
                return@setOnClickListener
            }

            if (weight < 20 || weight > 200) {
                showToast("몸무게는 20~200 범위로 입력해주세요.")
                return@setOnClickListener
            }

            if (gender == null) {
                showToast("성별을 선택해주세요.")
                return@setOnClickListener
            }

            val profilePart = selectedProfileUri?.let { uri ->
                ImageMultipartUtil.uriToCompressedMultipart(
                    context = requireContext(),
                    uri = uri,
                    partName = "profilePhoto",
                    maxBytes = 600 * 1024,
                    maxDimension = 1280
                )
            }

            val bodyPart = selectedBodyUri?.let { uri ->
                ImageMultipartUtil.uriToCompressedMultipart(
                    context = requireContext(),
                    uri = uri,
                    partName = "bodyPhoto",
                    maxBytes = 900 * 1024,
                    maxDimension = 1600
                )
            }

            val dataObj = EditProfileUpdateData(
                nickname = nickname,
                height = height,
                weight = weight,
                gender = gender!!,
                alarmEnabled = binding.switchAlarm.isChecked
            )

            val json = Gson().toJson(dataObj)

            Log.d(
                TAG,
                "updateProfile: userId=$userId, profilePhoto=$selectedProfileUri, bodyPhoto=$selectedBodyUri, data=$json"
            )

            viewModel.updateProfileMultipart(
                userId = userId,
                profilePhoto = profilePart,
                bodyPhoto = bodyPart,
                nickname = nickname,
                height = height,
                weight = weight,
                gender = gender!!,
                alarmEnabled = binding.switchAlarm.isChecked
            )
        }

        binding.tvChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    // 성별 버튼 클릭 이벤트 처리임
    private fun setupGenderButtons() {
        binding.btnFemale.setOnClickListener {
            gender = "FEMALE"
            selectGender()
        }

        binding.btnMale.setOnClickListener {
            gender = "MALE"
            selectGender()
        }
    }

    // 성별 선택 UI 반영 처리임
    private fun selectGender() {
        if (gender == "FEMALE") {
            binding.btnFemale.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.main_color))
            binding.btnMale.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.gray_300))
        } else if (gender == "MALE") {
            binding.btnMale.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.main_color))
            binding.btnFemale.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.gray_300))
        }
    }

    // 비밀번호 변경 다이얼로그 표시 및 검증 처리임
    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile_password, null)

        val etNew = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirm = dialogView.findViewById<EditText>(R.id.etNewPasswordConfirm)

        val btnToggleNew = dialogView.findViewById<ImageButton>(R.id.btnToggleNewPassword)
        val btnToggleConfirm = dialogView.findViewById<ImageButton>(R.id.btnToggleNewPasswordConfirm)

        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmChangePassword)

        var newVisible = false
        var confirmVisible = false

        btnToggleNew.setOnClickListener {
            newVisible = togglePasswordVisibility(etNew, newVisible)
        }

        btnToggleConfirm.setOnClickListener {
            confirmVisible = togglePasswordVisibility(etConfirm, confirmVisible)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnConfirm.setOnClickListener {
            val newPw = etNew.text.toString()
            val confirmPw = etConfirm.text.toString()

            if (newPw.isBlank() || confirmPw.isBlank()) {
                showToast("모든 항목을 입력해주세요.")
                return@setOnClickListener
            }

            if (newPw != confirmPw) {
                showToast("새 비밀번호가 일치하지 않습니다.")
                return@setOnClickListener
            }

            if (newPw.length < 8) {
                showToast("비밀번호는 8자리 이상이어야 합니다.")
                return@setOnClickListener
            }

            viewModel.changePassword(
                newPassword = newPw,
                newPasswordConfirm = confirmPw
            )

            dialog.dismiss()
        }

        dialog.show()
    }

    // 비밀번호 표시 토글 처리임
    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean): Boolean {
        editText.inputType =
            if (isVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }

        editText.setSelection(editText.text.length)
        return !isVisible
    }
}
