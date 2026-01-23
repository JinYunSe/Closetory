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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentEditProfileBinding
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.util.AuthManager

class EditProfileFragment :
    BaseFragment<FragmentEditProfileBinding>(
        FragmentEditProfileBinding::bind,
        R.layout.fragment_edit_profile
    ) {
    private val viewModel: EditProfileViewModel by viewModels()

    // 성별 여부 확인
    private var isFemale: Boolean? = null

    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean): Boolean {
        if (isVisible) {
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }

        editText.setSelection(editText.text.length)
        return !isVisible
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUiEvents()
        setupGenderButtons()
        observeViewModel()

        loadUserProfile()
    }

    // 서버에 기존 유저 정보 요청 시작
    private fun loadUserProfile() {
        Log.d("EDIT_PROFILE", "loadUserProfile() called")
        val authManager = AuthManager(requireContext())
        val userId = authManager.getUserId() ?: return
        Log.d("loadUserProfile launch전", "loadUserProfile launch전")

        viewModel.loadUserProfile(
            userId = userId
        )
    }

    // ViewModel 결과 관찰
    private fun observeViewModel() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            bindUserProfile(user)
        }

        viewModel.message.observe(viewLifecycleOwner) {
            showToast(it)
        }
    }

    // UI에 기존 데이터 채우기
    private fun bindUserProfile(user: EditProfileInfoResponse) {
        Log.d("EDIT_PROFILE", "bindUserProfile called: $user")
        // 텍스트 정보
        binding.etNickname.setText(user.nickname)
        binding.etHeight.setText(user.height.toString())
        binding.etWeight.setText(user.weight.toString())
        binding.switchAlarm.isChecked = user.alarmEnabled

        isFemale = user.gender == "FEMALE"
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

    private fun initUiEvents() {
        // 취소 버튼 → 이전 화면
        binding.btnCancel.setOnClickListener {
            // Navigation 사용
            findNavController().popBackStack()
        }

        // 저장 버튼 → 아직 동작 안 함 (토스트만)
        binding.btnSave.setOnClickListener {
            showToast("저장 버튼 클릭됨")
        }

        // 비밀번호 변경 다이얼로그
        binding.tvChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    //    회원 정보 수정 : 성별 선택 버튼
    private fun setupGenderButtons() {
        binding.btnFemale.setOnClickListener {
            isFemale = true
            selectGender()
        }

        binding.btnMale.setOnClickListener {
            isFemale = false
            selectGender()
        }
    }

    // 회원 정보 수정 시 성별 선택 시 버튼 색 변경
    private fun selectGender() {
        if (isFemale == true) {
            binding.btnFemale.setBackgroundTintList(
                ColorStateList.valueOf(requireContext().getColor(R.color.main_color))
            )
            binding.btnMale.setBackgroundTintList(
                ColorStateList.valueOf(requireContext().getColor(R.color.gray_500))
            )
        } else {
            binding.btnMale.setBackgroundTintList(
                ColorStateList.valueOf(requireContext().getColor(R.color.main_color))
            )
            binding.btnFemale.setBackgroundTintList(
                ColorStateList.valueOf(requireContext().getColor(R.color.gray_500))
            )
        }
    }

    // 🔹🔹🔹🔹 다이얼로그 구현 🔹🔹🔹🔹
    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater
            .inflate(R.layout.dialog_edit_profile_password, null)

        // UI 구성 요소들
        val etCurrent = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val btnToggleCurrent =
            dialogView.findViewById<ImageButton>(R.id.btnToggleCurrentPassword)

        val etNew = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val btnToggleNew =
            dialogView.findViewById<ImageButton>(R.id.btnToggleNewPassword)

        val etConfirm = dialogView.findViewById<EditText>(R.id.etNewPasswordConfirm)
        val btnToggleConfirm =
            dialogView.findViewById<ImageButton>(R.id.btnToggleNewPasswordConfirm)

        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmChangePassword)

        // 비밀번호 Visable 토글 기능 구현
        var isCurrentVisible = false
        var isNewVisible = false
        var isConfirmVisible = false

        btnToggleCurrent.setOnClickListener {
            isCurrentVisible =
                togglePasswordVisibility(etCurrent, isCurrentVisible)
        }

        btnToggleNew.setOnClickListener {
            isNewVisible =
                togglePasswordVisibility(etNew, isNewVisible)
        }

        btnToggleConfirm.setOnClickListener {
            isConfirmVisible =
                togglePasswordVisibility(etConfirm, isConfirmVisible)
        }

        // 다이얼로그 띄우기
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnConfirm.setOnClickListener {
            val currentPw = etCurrent.text.toString()
            val newPw = etNew.text.toString()
            val confirmPw = etConfirm.text.toString()

            // 🔹 유효성 검사 (지금은 Fragment에서)
            if (currentPw.isBlank() || newPw.isBlank() || confirmPw.isBlank()) {
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

            // 👉 다음 단계: ViewModel로 전달
            showToast("비밀번호 변경 요청")
            dialog.dismiss()
        }
        dialog.show()
    }
}
