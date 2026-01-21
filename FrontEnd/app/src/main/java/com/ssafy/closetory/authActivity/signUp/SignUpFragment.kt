// SignUpFragment.kt

package com.ssafy.closetory.authActivity.signUp

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 회원가입 버튼
        binding.btnSignupOkay.setOnClickListener {
            val id = binding.etSignupId.text.toString()
            val pw = binding.etSignupPassword.text.toString()
            val pwConfirm =
                binding.etSignupPasswordConfirmation.text.toString()
            val nickname = binding.etSignupNickname.text.toString()

            Log.d(TAG, "회원가입 버튼 클릭")
            signUpViewModel.signUp(
                id,
                pw,
                pwConfirm,
                nickname
            )
        }

        // 메시지 토스트
        signUpViewModel.message.observe(viewLifecycleOwner) { msg ->
            showToast(msg)
        }

        // 회원가입 성공 → 로그인 화면으로 복귀
        signUpViewModel.signUpSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                parentFragmentManager.popBackStack()
            }
        }

        // 로그인 화면 이동
        binding.tvYesIdToLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
