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

    private var selectedGender: String? = null // "Male" or "Female"

    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean): Boolean {
        if (isVisible) {
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }

        // 커서 맨 뒤 유지
        editText.setSelection(editText.text.length)

        return !isVisible
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 성별 버튼
        binding.btnMale.setOnClickListener {
            selectedGender = "MALE"
            binding.btnMale.isSelected = true
            binding.btnMale.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.main_color))
            binding.btnFemale.isSelected = false
            binding.btnFemale.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.gray_500))
        }

        binding.btnFemale.setOnClickListener {
            selectedGender = "FEMALE"
            binding.btnFemale.isSelected = true
            binding.btnFemale.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.main_color))
            binding.btnMale.isSelected = false
            binding.btnMale.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.gray_500))
        }

        // 회원가입 버튼
        binding.btnSignupOkay.setOnClickListener {
            Log.d(TAG, "회원가입 버튼 클릭")

            val alarmEnabled = binding.switchAlarm.isChecked

            signUpViewModel.signUp(
                username = binding.etSignupId.text.toString(),
                password = binding.etSignupPassword.text.toString(),
                passwordConfirm = binding.etSignupPasswordConfirmation.text.toString(),
                nickname = binding.etSignupNickname.text.toString(),
                gender = selectedGender,
                heightText = binding.etPersonalHeight.text.toString(),
                weightText = binding.etPersonalWeight.text.toString(),
                alarmEnabled = alarmEnabled
            )
        }

        // 토스트 메시지 수신
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                signUpViewModel.message.collect { msg ->
                    if (msg.isNotBlank()) showToast(msg)
                }
            }
        }

        // 성공 이벤트 수신
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                signUpViewModel.signUpSuccess.collect { success ->
                    if (success) parentFragmentManager.popBackStack()
                }
            }
        }
        // 로그인 이동
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
                togglePasswordVisibility(binding.etSignupPasswordConfirmation, isPasswordConfirmVisible)
        }
    }
}
