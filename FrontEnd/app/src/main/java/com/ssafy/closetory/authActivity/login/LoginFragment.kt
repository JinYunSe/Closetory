// LoginFragment.kt

package com.ssafy.closetory.authActivity.login

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.ssafy.closetory.R
import com.ssafy.closetory.authActivity.signUp.SignUpFragment
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentLoginBinding

private const val TAG = "LoginFragment_싸피"

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

            Log.d("LOGIN_FLOW", "로그인 버튼 클릭: $username / $password")

            loginViewModel.login(username, password)
        }

        loginViewModel.message.observe(viewLifecycleOwner) { msg ->
            Log.d("LOGIN_FLOW", "Fragment에서 message observe됨: $msg")
            showToast(msg)
        }

        loginViewModel.loginData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                // TODO: HomeActivity 이동
            }
        }
    }
}
