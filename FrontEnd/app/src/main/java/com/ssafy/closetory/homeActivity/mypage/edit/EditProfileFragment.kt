package com.ssafy.closetory.homeActivity.mypage.edit

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentEditProfileBinding

class EditProfileFragment :
    BaseFragment<FragmentEditProfileBinding>(
        FragmentEditProfileBinding::bind,
        R.layout.fragment_edit_profile
    ) {
    // 성별 여부 확인
    private var isFemale: Boolean? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ❌ 서버 아직 안 씀
        // ❌ ViewModel 아직 안 씀

        initUiEvents()
        setupGenderButtons()
    }

    private fun initUiEvents() {
        // 취소 버튼 → 이전 화면
        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
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
