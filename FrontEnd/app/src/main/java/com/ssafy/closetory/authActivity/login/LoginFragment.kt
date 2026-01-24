// LoginFragment.kt

package com.ssafy.closetory.authActivity.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.authActivity.signUp.SignUpFragment
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentLoginBinding
import com.ssafy.closetory.homeActivity.HomeActivity

private const val TAG = "LoginFragment_싸피"

private var isLoginPasswordVisible = false

class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::bind, R.layout.fragment_login) {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 회원가입 화면 이동
        binding.tvNoIdToSignup.setOnClickListener {
            Log.d(TAG, "SignUp 이동 버튼 : 동작 유무 확인")
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, SignUpFragment())
                .addToBackStack(null)
                .commit()
        }

        // 로그인 버튼 클릭 → ViewModel 호출
        binding.btnLogin.setOnClickListener {
            val username = binding.etLoginId.text.toString()
            val password = binding.etLoginPassword.text.toString()

            if (username.isBlank() || password.isBlank()) {
                showToast("아이디 비밀번호를 입력해주세요")
                return@setOnClickListener
            }

            Log.d("LOGIN_FLOW", "로그인 버튼 클릭: $username / $password")

            loginViewModel.login(username, password)
        }

        loginViewModel.message.observe(viewLifecycleOwner) { msg ->
            Log.d("LOGIN_FLOW", "Fragment에서 message observe됨: $msg")
            showToast(msg)
        }

        loginViewModel.loginData.observe(viewLifecycleOwner) { data ->

            if (data == null) return@observe

            val authManager = ApplicationClass.authManager
            Log.d(TAG, "로그인 동작 확인 : accessToken : ${data.accessToken}, refreshToken : ${data.refreshToken}")

            authManager.saveTokens(data.accessToken, data.refreshToken)

//            ApplicationClass.sharedPreferences.putUserId("userId", data.userId)

            Log.d(TAG, "HomeActivity 이동 버튼 동작 유무 확인")

            val intent = Intent(requireContext(), HomeActivity::class.java).apply {
                // 기존 작업 태스크를 모두 비우고 새로운 태스크로 HomeActivity를 실행
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
        binding.btnToggleLoginPassword.setOnClickListener {
            isLoginPasswordVisible = !isLoginPasswordVisible

            binding.etLoginPassword.inputType =
                if (isLoginPasswordVisible) {
                    InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                } else {
                    InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                }

            // 커서 맨 뒤 유지
            binding.etLoginPassword.setSelection(
                binding.etLoginPassword.text.length
            )
        }
    }
}
