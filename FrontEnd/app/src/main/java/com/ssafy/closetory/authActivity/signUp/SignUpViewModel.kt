// SignUpViewModel.kt

package com.ssafy.closetory.authActivity.signUp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.SignUpRequest
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {

    private val repository = SignUpRepository()

    private val _signUpSuccess = MutableLiveData<Boolean>()
    val signUpSuccess: LiveData<Boolean> = _signUpSuccess

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun signUp(
        username: String,
        password: String,
        passwordConfirm: String,
        nickname: String,
        gender: String?,
        heightText: String,
        weightText: String,
        alarmEnabled: Boolean
    ) {
        // 1) 필수값 검증
        if (
            username.isBlank() ||
            password.isBlank() ||
            passwordConfirm.isBlank() ||
            nickname.isBlank()
        ) {
            _message.value = "필수 항목을 모두 입력해주세요."
            return
        }

        // 2) 비밀번호 검증
        if (password.length < 8) {
            _message.value = "비밀번호는 8자 이상이어야 합니다."
            return
        }
        if (password != passwordConfirm) {
            _message.value = "비밀번호가 일치하지 않습니다."
            return
        }

        // 3) 키/몸무게 숫자 변환
        val height = heightText.toIntOrNull()
        val weight = weightText.toIntOrNull()

        if (height == null || weight == null) {
            _message.value = "키와 몸무게는 숫자로 입력해주세요."
            return
        }
        if (height < 0 || height > 400) {
            _message.value = "키는 가능 범위로 입력하여주세요. (0~400)"
            return
        }
        if (weight < 0 || weight > 800) {
            _message.value = "몸무게는 가능 범위로 입력하여주세요. (0~800)"
            return
        }

        // 4) 성별 선택 여부
        if (gender.isNullOrBlank()) {
            _message.value = "성별을 선택해주세요."
            return
        }

        viewModelScope.launch {
            try {
                val request = SignUpRequest(
                    username = username,
                    password = password,
                    passwordConfirm = passwordConfirm,
                    nickname = nickname,
                    gender = gender,
                    height = height,
                    weight = weight,
                    alarmEnabled = alarmEnabled
                )

                val res = repository.signUp(request)

                Log.d("SIGNUP_FLOW", "HTTP code = ${res.code()}, body=${res.body()}")

                val body = res.body()

                if (res.isSuccessful) {
                    _signUpSuccess.value = true
                    _message.value = body?.responseMessage ?: "회원가입 성공"
                } else {
                    _signUpSuccess.value = false
                    _message.value =
                        body?.errorMessage ?: body?.responseMessage ?: "알 수 없는 오류로 회원가입 할 수 없습니다."
                }
            } catch (e: Exception) {
                Log.e("SIGNUP_FLOW", "signUp() 예외 발생 ${e.message}", e)
                _signUpSuccess.value = false
                _message.value = "알 수 없는 오류로 회원가입 할 수 없습니다."
            }
        }
    }
}
