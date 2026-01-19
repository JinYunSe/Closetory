package com.ssafy.closetory.authActivity.signUp

import android.os.Bundle
import android.util.Log
import android.view.View
import com.ssafy.closetory.R
import com.ssafy.closetory.authActivity.login.LoginFragment
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentSignUpBinding

private const val TAG = "SignUpFragment_싸피"
class SignUpFragment : BaseFragment<FragmentSignUpBinding>(FragmentSignUpBinding::bind, R.layout.fragment_sign_up) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            Log.d(TAG, "Login 버튼 : 동작 유무 확인")
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
