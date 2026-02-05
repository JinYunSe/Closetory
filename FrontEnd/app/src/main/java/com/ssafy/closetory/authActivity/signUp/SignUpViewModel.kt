package com.ssafy.closetory.authActivity.signUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.SignUpRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {

    private val repository = SignUpRepository()

    // 1회성 이벤트
    private val _signUpSuccess = MutableSharedFlow<Boolean>(replay = 0)
    val signUpSuccess: SharedFlow<Boolean> = _signUpSuccess

    private val _message = MutableSharedFlow<String>(replay = 0)
    val message: SharedFlow<String> = _message

    fun signUp(
        username: String,
        password: String,
        passwordConfirm: String,
        nickname: String,
        gender: String,
        height: Short,
        weight: Short,
        alarmEnabled: Boolean
    ) {
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
                val body = res.body()

                if (res.isSuccessful) {
                    _signUpSuccess.emit(true)
                    _message.emit(body?.responseMessage ?: "회원가입 성공")
                } else {
                    _signUpSuccess.emit(false)
                    _message.emit(
                        body?.errorMessage ?: "입력 정보를 확인해 주세요."
                    )
                }
            } catch (e: Exception) {
                _message.emit("회원가입에 실패했습니다.")
                _signUpSuccess.emit(false)
            }
        }
    }
}
