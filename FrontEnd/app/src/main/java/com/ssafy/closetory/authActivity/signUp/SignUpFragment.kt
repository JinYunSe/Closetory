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

    // 아이디: 영문 대/소문자, 숫자, 특수문자만 허용 (공백/한글/기타 불가)
    // 허용 특수문자는 보수적으로 많이 쓰는 것들로 잡음 (원하면 조정 가능)
    private val USERNAME_REGEX = Regex("^[A-Za-z0-9!@#\$%^&*()_+\\-={}:;\"'<>,.?/\\\\|\\[\\]]+$")

    // 비밀번호: 영문(대/소 아무거나) + 숫자 + 특수문자 포함, 길이 8 이상
    private val PASSWORD_REGEX =
        Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-={}:;\"'<>,.?/\\\\|\\[\\]]).{8,}$")

    // 닉네임: 특수문자 금지 (영문/숫자/한글/공백/언더바 등 허용하고 싶으면 이렇게)
    // 공백까지 막고 싶으면 \\s 를 빼면 됨
    private val NICKNAME_REGEX = Regex("^[A-Za-z0-9가-힣\\s_]+$")

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
                ColorStateList.valueOf(requireContext().getColor(R.color.gray_300))
        }

        binding.btnFemale.setOnClickListener {
            selectedGender = "FEMALE"
            binding.btnFemale.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.main_color))
            binding.btnMale.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(R.color.gray_300))
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
            val height = heightText.toShortOrNull()
            val weight = weightText.toShortOrNull()

            when {
                username.isBlank() ->
                    showToast("아이디를 입력해 주세요.")

                !USERNAME_REGEX.matches(username) ->
                    showToast("아이디는 영문 대소문자, 숫자, 특수문자만 사용할 수 있습니다.")

                password.isBlank() ->
                    showToast("비밀번호를 입력해 주세요.")

                passwordConfirm.isBlank() ->
                    showToast("비밀번호 확인을 입력해 주세요.")

                password != passwordConfirm ->
                    showToast("비밀번호가 일치하지 않습니다.")

                // 기존 length < 8 메시지 유지하고 싶으면 먼저 두고,
                // "모두 포함"은 그 다음 체크해도 됨
                password.length < 8 ->
                    showToast("비밀번호는 8자리 이상으로 입력해 주세요.")

                !PASSWORD_REGEX.matches(password) ->
                    showToast("비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.")

                nickname.isBlank() ->
                    showToast("닉네임을 입력해 주세요.")

                nickname.length > 10 ->
                    showToast("닉네임은 최대 10자까지 가능합니다.")

                !NICKNAME_REGEX.matches(nickname) ->
                    showToast("닉네임에는 특수문자를 사용할 수 없습니다.")

                heightText.isBlank() ->
                    showToast("키를 입력해 주세요.")

                height == null ->
                    showToast("키를 입력해 주세요.")

                (height < 100 || height > 250) ->
                    showToast("키는 100~250 범위로 입력해 주세요.")

                weightText.isBlank() ->
                    showToast("몸무게를 입력해 주세요.")

                weight == null ->
                    showToast("몸무게를 입력해 주세요.")

                (weight < 20 || weight > 200) ->
                    showToast("몸무게는 20~200 범위로 입력해 주세요.")

                selectedGender == null ->
                    showToast("성별을 선택해 주세요.")

                else -> {
                    signUpViewModel.signUp(
                        username = username,
                        password = password,
                        passwordConfirm = passwordConfirm,
                        nickname = nickname,
                        gender = selectedGender!!,
                        height = height,
                        weight = weight,
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



