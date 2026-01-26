// EditProfileFragment
package com.ssafy.closetory.homeActivity.mypage.edit

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentEditProfileBinding
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.homeActivity.edit.PreferenceTagsSurveyDialogFragment
import kotlinx.coroutines.launch

private const val TAG = "EditProfileFragment_싸피"

class EditProfileFragment :
    BaseFragment<FragmentEditProfileBinding>(
        FragmentEditProfileBinding::bind,
        R.layout.fragment_edit_profile
    ) {

    private val viewModel: EditProfileViewModel by viewModels()

    // 성별 상태 (FEMALE : 여성, MALE : 남성)
    private var gender: String? = null

    // 서버에서 내려온 기존 값
    private var profilePhotoUrl: String? = null
    private var bodyPhotoUrl: String? = null

    // 선호 태그(선택된 코드들)
    private var selectedPreferenceTagCodes: List<Int> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 서버에 기존 유저 정보 요청
        clickListeners()

        // 성별 버튼 이벤트
        setupGenderButtons()

        // ViewModel 이벤트 수신
        observeViewModel()

        // 선호 태그 다이얼로그 결과 받기
        setupPreferenceTagResultListener()

        // 서버에 기존 유저 정보 요청
        loadUserProfile()
    }

    // 서버에 기존 유저 정보 요청
    private fun loadUserProfile() {
        Log.d(TAG, "loadUserProfile: loadUserProfile 실행")
        val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID) ?: return
        viewModel.loadUserProfile(userId)
    }

    // ViewModel 데이터 / 메시지 수신
    private fun observeViewModel() {
        // 회원정보 수신
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userProfile.collect { user ->
                bindUserProfile(user)
            }
        }

        // 메시지 수신 (성공 / 실패 공통)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.message.collect { message ->
                showToast(message)

                // 회원정보 수정 성공 후 뒤로가기 정책
                if (message.contains("수정")) {
                    findNavController().popBackStack()
                }
            }
        }
    }

    // UI에 회원정보 바인딩
    private fun bindUserProfile(user: EditProfileInfoResponse) {
        binding.etNickname.setText(user.nickname)
        binding.etHeight.setText(user.height?.toString().orEmpty())
        binding.etWeight.setText(user.weight?.toString().orEmpty())
        binding.switchAlarm.isChecked = user.alarmEnabled
        gender = user.gender
        profilePhotoUrl = user.profilePhotoUrl
        bodyPhotoUrl = user.bodyPhotoUrl
        selectGender()

        // 프로필 사진
        if (user.profilePhotoUrl.isNullOrBlank()) {
            binding.imgProfile.setImageResource(R.drawable.ic_profile_default)
            binding.tvProfilePlaceholder.visibility = View.VISIBLE
        } else {
            binding.tvProfilePlaceholder.visibility = View.GONE
            Glide.with(this)
                .load(user.profilePhotoUrl)
                .placeholder(R.drawable.ic_profile_default)
                .error(R.drawable.ic_profile_default)
                .into(binding.imgProfile)
        }

        // 전신 사진
        if (user.bodyPhotoUrl.isNullOrBlank()) {
            binding.imgBody.setImageResource(R.drawable.ic_body_default)
            binding.tvBodyPlaceholder.visibility = View.VISIBLE
        } else {
            binding.tvBodyPlaceholder.visibility = View.GONE
            Glide.with(this)
                .load(user.bodyPhotoUrl)
                .placeholder(R.drawable.ic_body_default)
                .error(R.drawable.ic_body_default)
                .into(binding.imgBody)
        }
    }

    // 선호 태그 조사 버튼 클릭 이벤트
    private fun setupPreferenceTagResultListener() {
        parentFragmentManager.setFragmentResultListener("pref_tags_result", viewLifecycleOwner) { _, bundle ->
            val codes = bundle.getIntArray("codes")?.toList().orEmpty()
            selectedPreferenceTagCodes = codes

            // (선택) 화면에 표시하고 싶으면 여기서 TextView 갱신
            // binding.tvChangePreferenceTags.text = "선호 태그 (${codes.size})"
            Log.d(TAG, "선호 태그 선택 결과: $codes")
        }
    }

    // 클릭 이벤트 및 입력 검증
    private fun clickListeners() {
        // 취소
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        // 저장
        binding.btnSave.setOnClickListener {
            val nickname = binding.etNickname.text.toString().trim()
            val heightText = binding.etHeight.text.toString().trim()
            val weightText = binding.etWeight.text.toString().trim()

            if (nickname.isBlank()) {
                showToast("닉네임을 입력해주세요.")
                return@setOnClickListener
            }

            val height = heightText.toIntOrNull()
            val weight = weightText.toIntOrNull()

            if (height == null) {
                showToast("키는 숫자로 입력해주세요.")
                return@setOnClickListener
            }

            if (weight == null) {
                showToast("몸무게는 숫자로 입력해주세요.")
                return@setOnClickListener
            }

            if (gender == null) {
                showToast("성별을 선택해주세요.")
                return@setOnClickListener
            }

            viewModel.updateProfile(
                nickname = nickname,
                height = height,
                weight = weight,
                gender = gender!!,
                alarmEnabled = binding.switchAlarm.isChecked,
                profilePhotoUrl = profilePhotoUrl,
                bodyPhotoUrl = bodyPhotoUrl
            )
        }

        // 비밀번호 변경
        binding.tvChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // 선호 태그 변경
        binding.tvChangePreferenceTags.setOnClickListener {
            PreferenceTagsSurveyDialogFragment()
                .show(parentFragmentManager, "pref_tags_dialog")
        }
    }

    // 성별 버튼 이벤트
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

    // 성별 선택 UI 처리
    private fun selectGender() {
        if (gender == "FEMALE") {
            binding.btnFemale.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.main_color))
            binding.btnMale.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.gray_500))
        } else if (gender == "MALE") {
            binding.btnMale.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.main_color))
            binding.btnFemale.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.gray_500))
        }
    }

    // 비밀번호 변경 다이얼로그
    private fun showChangePasswordDialog() {
        val dialogView =
            layoutInflater.inflate(R.layout.dialog_edit_profile_password, null)

        val etNew = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirm = dialogView.findViewById<EditText>(R.id.etNewPasswordConfirm)

        val btnToggleNew =
            dialogView.findViewById<ImageButton>(R.id.btnToggleNewPassword)
        val btnToggleConfirm =
            dialogView.findViewById<ImageButton>(R.id.btnToggleNewPasswordConfirm)

        val btnConfirm =
            dialogView.findViewById<Button>(R.id.btnConfirmChangePassword)

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

    // 비밀번호 표시/숨김 토글
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
