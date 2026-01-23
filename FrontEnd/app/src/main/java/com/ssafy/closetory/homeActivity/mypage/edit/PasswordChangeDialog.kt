// package com.ssafy.closetory.homeActivity.mypage.edit
//
// import androidx.fragment.app.DialogFragment
//
// class PasswordChangeDialog : DialogFragment() {
//
//    private lateinit var binding: DialogChangePasswordBinding
//    private val viewModel: EditProfileViewModel by activityViewModels()
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        binding = DialogChangePasswordBinding.inflate(layoutInflater)
//
//        return AlertDialog.Builder(requireContext())
//            .setView(binding.root)
//            .setPositiveButton("확인") { _, _ ->
//                viewModel.changePassword(
//                    currentPassword = binding.etCurrentPassword.text.toString(),
//                    newPassword = binding.etNewPassword.text.toString(),
//                    confirmPassword = binding.etNewPasswordConfirm.text.toString()
//                )
//            }
//            .setNegativeButton("취소", null)
//            .create()
//    }
// }
