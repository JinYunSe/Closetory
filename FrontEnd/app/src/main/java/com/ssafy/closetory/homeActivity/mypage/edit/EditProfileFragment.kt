// package com.ssafy.closetory.homeActivity.mypage.edit
//
// import android.os.Bundle
// import android.view.View
// import androidx.fragment.app.viewModels
// import com.ssafy.closetory.R
// import com.ssafy.closetory.baseCode.base.BaseFragment
// import com.ssafy.closetory.databinding.FragmentEditProfileBinding
//
// class EditProfileFragment :
//    BaseFragment<FragmentEditProfileBinding>(
//        FragmentEditProfileBinding::bind,
//        R.layout.fragment_edit_profile
//    ) {
//
//    private val viewModel: EditProfileViewModel by viewModels()
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // 비밀번호 변경 다이얼로그
//        binding.tvChangePassword.setOnClickListener {
//            PasswordChangeDialog().show(
//                parentFragmentManager,
//                "PasswordChangeDialog"
//            )
//        }
//
//        // 회원정보 수정
//        binding.btnConfirm.setOnClickListener {
//            viewModel.updateProfile(
//                nickname = binding.etNickname.text.toString(),
//                gender = viewModel.selectedGender,
//                height = binding.etPersonalHeight.text.toString(),
//                weight = binding.etPersonalWeight.text.toString(),
//                alarmEnabled = binding.switchAlarm.isChecked
//            )
//        }
//
//        observeViewModel()
//    }
// }
