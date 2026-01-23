package com.ssafy.closetory.homeActivity.mypage.edit

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ❌ 서버 아직 안 씀
        // ❌ ViewModel 아직 안 씀

        initUiEvents()
        setupGenderButtons()
        observeViewModel()

        loadUserProfile()
    }

    // 🔹 서버 요청 시작
    private fun loadUserProfile() {
        Log.d("EDIT_PROFILE", "loadUserProfile() called")
        val authManager = AuthManager(requireContext())
        val userId = authManager.getUserId() ?: return
        Log.d("loadUserProfile launch전", "loadUserProfile launch전")

        viewModel.loadUserProfile(
            userId = userId
        )
    }

    // 🔹 ViewModel 결과 관찰
    private fun observeViewModel() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            bindUserProfile(user)
        }

        viewModel.message.observe(viewLifecycleOwner) {
            showToast(it)
        }
    }

    // 🔹 UI에 데이터 채우기
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

        // 비밀번호 변경 → 아직 Dialog만 띄움
        binding.tvChangePassword.setOnClickListener {
            showToast("비밀번호 변경 클릭")
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
}
