// SignUpFragment.kt

package com.ssafy.closetory.authActivity.signUp

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ssafy.closetory.R
import com.ssafy.closetory.baseCode.base.BaseFragment
import com.ssafy.closetory.databinding.FragmentSignUpBinding
import kotlinx.coroutines.launch

private const val TAG = "SignUpFragment_싸피"

class SignUpFragment :
    BaseFragment<FragmentSignUpBinding>(
        FragmentSignUpBinding::bind,
        R.layout.fragment_sign_up
    ) {

    private val signUpViewModel: SignUpViewModel by viewModels()

    private var selectedGender: String? = null // "MALE" | "FEMALE"

    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean): Boolean {
        editText.inputType =
            if (isVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }

        editText.setSelection(editText.text.length)
        return !isVisible
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 성별 버튼
        binding.btnMale.setOnClickListener {
            selectedGender = "MALE"
            binding.btnMale.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.main_color))
            binding.btnFemale.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.gray_500))
        }

        binding.btnFemale.setOnClickListener {
            selectedGender = "FEMALE"
            binding.btnFemale.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.main_color))
            binding.btnMale.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.gray_500))
        }

        // 회원가입 버튼
        binding.btnSignupOkay.setOnClickListener {
            Log.d(TAG, "회원가입 버튼 클릭")

            val username = binding.etSignupId.text.toString().trim()
            val password = binding.etSignupPassword.text.toString().trim()
            val passwordConfirm = binding.etSignupPasswordConfirmation.text.toString().trim()
            val nickname = binding.etSignupNickname.text.toString().trim()
            val heightText = binding.etPersonalHeight.text.toString().trim()
            val weightText = binding.etPersonalWeight.text.toString().trim()
            val alarmEnabled = binding.switchAlarm.isChecked

            // ====== 입력값 검증 ======
            when {
                username.isBlank() ->
                    showToast("아이디를 입력하세요")

                password.isBlank() ->
                    showToast("비밀번호를 입력하세요")

                passwordConfirm.isBlank() ->
                    showToast("비밀번호 확인을 입력하세요")

                password != passwordConfirm ->
                    showToast("비밀번호가 일치하지 않습니다")

                nickname.isBlank() ->
                    showToast("닉네임을 입력하세요")

                heightText.isBlank() ->
                    showToast("키를 입력하세요")

                weightText.isBlank() ->
                    showToast("몸무게를 입력하세요")

                selectedGender == null ->
                    showToast("성별을 선택하세요")

                heightText.toIntOrNull() == null ->
                    showToast("키를 입력하세요")

                weightText.toIntOrNull() == null ->
                    showToast("몸무게를 입력하세요")

                else -> {
                    // 검증 통과
                    signUpViewModel.signUp(
                        username = username,
                        password = password,
                        passwordConfirm = passwordConfirm,
                        nickname = nickname,
                        gender = selectedGender!!,
                        height = heightText.toInt(),
                        weight = weightText.toInt(),
                        alarmEnabled = alarmEnabled
                    )
                }
            }
        }

        // 메시지 수신
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                signUpViewModel.message.collect {
                    if (it.isNotBlank()) showToast(it)
                }
            }
        }

        // 성공 처리
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                signUpViewModel.signUpSuccess.collect {
                    if (it) parentFragmentManager.popBackStack()
                }
            }
        }

        binding.tvYesIdToLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        var isPasswordVisible = false
        var isPasswordConfirmVisible = false

        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible =
                togglePasswordVisibility(binding.etSignupPassword, isPasswordVisible)
        }

        binding.btnTogglePasswordConfirmation.setOnClickListener {
            isPasswordConfirmVisible =
                togglePasswordVisibility(
                    binding.etSignupPasswordConfirmation,
                    isPasswordConfirmVisible
                )
        }
    }
}
