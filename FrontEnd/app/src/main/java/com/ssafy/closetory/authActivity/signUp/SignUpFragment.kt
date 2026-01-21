// SignUpFragment.kt

package com.ssafy.closetory.authActivity.signUp

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentSignUpBinding

private const val TAG = "SignUpFragment_싸피"

class SignUpFragment :
    BaseFragment<FragmentSignUpBinding>(
        FragmentSignUpBinding::bind,
        R.layout.fragment_sign_up
    ) {

    private val signUpViewModel: SignUpViewModel by viewModels()

    private var selectedGender: String? = null // "Male" or "Female"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 성별 버튼
        binding.btnMale.setOnClickListener {
            selectedGender = "Male"
            binding.btnMale.isSelected = true
            binding.btnMale.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.main_color))
            binding.btnFemale.isSelected = false
            binding.btnFemale.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.gray_500))
        }

        binding.btnFemale.setOnClickListener {
            selectedGender = "Female"
            binding.btnFemale.isSelected = true
            binding.btnFemale.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.main_color))
            binding.btnMale.isSelected = false
            binding.btnMale.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.gray_500))
        }

        // 회원가입 버튼
        binding.btnSignupOkay.setOnClickListener {
            Log.d(TAG, "회원가입 버튼 클릭")

            signUpViewModel.signUp(
                username = binding.etSignupId.text.toString(),
                password = binding.etSignupPassword.text.toString(),
                passwordConfirm = binding.etSignupPasswordConfirmation.text.toString(),
                nickname = binding.etSignupNickname.text.toString(),
                gender = selectedGender,
                heightText = binding.etPersonalHeight.text.toString(),
                weightText = binding.etPersonalWeight.text.toString()
            )
        }

        // 토스트 메시지
        signUpViewModel.message.observe(viewLifecycleOwner) { msg ->
            showToast(msg)
        }

        // 성공 시 로그인 화면 복귀
        signUpViewModel.signUpSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                parentFragmentManager.popBackStack()
            }
        }

        // 로그인 이동
        binding.tvYesIdToLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
