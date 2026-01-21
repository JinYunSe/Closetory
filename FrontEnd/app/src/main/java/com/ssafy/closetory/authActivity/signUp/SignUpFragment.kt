package com.ssafy.closetory.authActivity.signUp

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import com.ssafy.closetory.R
import com.ssafy.closetory.authActivity.login.LoginFragment
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentSignUpBinding

private const val TAG = "SignUpFragment_싸피"
class SignUpFragment : BaseFragment<FragmentSignUpBinding>(FragmentSignUpBinding::bind, R.layout.fragment_sign_up) {

    private var selectedGender: String? = null // 남,여 선택 버튼 토글화 하기 위함.

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGenderButtons()

        binding.tvYesIdToLogin.setOnClickListener {
            Log.d(TAG, "Login 버튼 : 동작 유무 확인")
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupGenderButtons() {
        binding.btnFemale.setOnClickListener {
            selectGender("FEMALE")
        }

        binding.btnMale.setOnClickListener {
            selectGender("MALE")
        }
    }

    private fun selectGender(gender: String) {
        selectedGender = gender

        val gray = requireContext().getColor(R.color.gray_500)
        val sky = requireContext().getColor(R.color.main_color)

        binding.btnFemale.backgroundTintList =
            ColorStateList.valueOf(gray)
        binding.btnMale.backgroundTintList =
            ColorStateList.valueOf(gray)

        when (gender) {
            "FEMALE" ->
                binding.btnFemale.backgroundTintList =
                    ColorStateList.valueOf(sky)

            "MALE" ->
                binding.btnMale.backgroundTintList =
                    ColorStateList.valueOf(sky)
        }
    }
}
