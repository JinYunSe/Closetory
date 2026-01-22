// LoginFragment.kt

package com.ssafy.closetory.authActivity.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.authActivity.signUp.SignUpFragment
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentLoginBinding
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.closetory.util.AuthManager

private const val TAG = "LoginFragment_싸피"

class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::bind, R.layout.fragment_login) {

    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var authManager: AuthManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 회원가입 화면 이동
        binding.tvNoIdToSignup.setOnClickListener {
            Log.d(TAG, "SignUp 이동 버튼 : 동작 유무 확인")
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, SignUpFragment())
                .commit()
        }

        // 로그인 버튼 클릭 → ViewModel 호출
        binding.btnLogin.setOnClickListener {
            val username = binding.etLoginId.text.toString()
            val password = binding.etLoginPassword.text.toString()

            Log.d("LOGIN_FLOW", "로그인 버튼 클릭: $username / $password")

            loginViewModel.login(username, password)
        }

        loginViewModel.message.observe(viewLifecycleOwner) { msg ->
            Log.d("LOGIN_FLOW", "Fragment에서 message observe됨: $msg")
            showToast(msg)
        }

        loginViewModel.loginData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                // 해더에 토큰 넣기

                authManager = ApplicationClass.authManager
                authManager.saveAccessToken(data.accessToken)
                authManager.saveRefreshToken(data.refreshToken)

                binding.btnLogin.setOnClickListener {
                    Log.d(TAG, "HomeActivity 이동 버튼 동작 유무 확인")

                    val intent = Intent(requireContext(), HomeActivity::class.java).apply {
                        // 기존 작업 태스크를 모두 비우고 새로운 태스크로 HomeActivity를 실행
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }
            }
        }
    }
}
