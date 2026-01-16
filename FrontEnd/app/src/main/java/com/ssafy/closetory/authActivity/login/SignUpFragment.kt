package com.ssafy.closetory.authActivity.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.FragmentSignUpBinding
import com.ssafy.ssafyfinalproject.baseCode.base.BaseFragment

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
