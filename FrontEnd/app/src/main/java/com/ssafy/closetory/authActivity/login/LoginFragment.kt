package com.ssafy.closetory.authActivity.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.ssafy.closetory.R
import com.ssafy.closetory.authActivity.signUp.SignUpFragment
import com.ssafy.closetory.databinding.FragmentLoginBinding
import com.ssafy.closetory.homeActivity.HomeActivity
import com.ssafy.ssafyfinalproject.baseCode.base.BaseFragment

private const val TAG = "LoginFragment_싸피"

class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::bind, R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignUp.setOnClickListener {
            Log.d(TAG, "SignUp 이동 버튼 : 동작 유무 확인")
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, SignUpFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnLogin.setOnClickListener {
            Log.d(TAG, "HomeActivity 이동 버튼 동작 유무 확인")
            startActivity(Intent(requireContext(), HomeActivity::class.java))
        }
    }
}
