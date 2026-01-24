package com.ssafy.closetory.authActivity.signUp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.SignUpRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class SignUpViewModel : ViewModel() {

    private val repository = SignUpRepository()

    // 1회성 이벤트: replay=0
    private val _signUpSuccess = MutableSharedFlow<Boolean>(replay = 0)
    val signUpSuccess: SharedFlow<Boolean> = _signUpSuccess

    private val _message = MutableSharedFlow<String>(replay = 0)
    val message: SharedFlow<String> = _message

    private fun toast(msg: String) {
        viewModelScope.launch { _message.emit(msg) }
    }

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
        Log.d("SIGNUP_FLOW", "ViewModel signUp() 진입")

        // 1. 필수값 검증
        if (username.isBlank() || password.isBlank() || passwordConfirm.isBlank() || nickname.isBlank()) {
            toast("필수 항목을 모두 입력해주세요.")
            return
        }

        // 2. 비밀번호 검증
        if (password.length < 8) {
            toast("비밀번호는 8자 이상이어야 합니다.")
            return
        }
        if (password != passwordConfirm) {
            toast("비밀번호가 일치하지 않습니다.")
            return
        }

        // 3. 키 / 몸무게 숫자 변환
        val height = heightText.toIntOrNull()
        val weight = weightText.toIntOrNull()

        if (height == null || weight == null) {
            toast("키와 몸무게는 숫자로 입력해주세요.")
            return
        }
        if (height < 0 || height > 400) {
            toast("키는 가능 범위로 입력하여주세요. (0~400)")
            return
        }
        if (weight < 0 || weight > 800) {
            toast("몸무게는 가능 범위로 입력하여주세요. (0~800)")
            return
        }

        // 4. 성별 선택 여부
        if (gender == null) {
            toast("성별을 선택해주세요.")
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

                Log.d("SIGNUP_REQUEST", request.toString())

                val res = repository.signUp(request)
                val errorJson = res.errorBody()?.string()

                Log.d("DEBUG", "################")
                Log.d("SIGNUP_FLOW", "HTTP code = ${res.code()}")
                Log.d("SIGNUP_FLOW", "response body = ${res.body()}")
                Log.d("SIGNUP_FLOW", "error body = $errorJson")
                Log.d("DEBUG", "################")

                if (res.isSuccessful) {
                    _signUpSuccess.emit(true)
                    _message.emit("회원가입 성공")
                } else {
                    val errorMessage = try {
                        if (errorJson.isNullOrBlank()) {
                            null
                        } else {
                            JSONObject(errorJson).optString("errorMessage", null)
                        }
                    } catch (_: Exception) {
                        null
                    }

                    _message.emit(errorMessage ?: "알 수 없는 오류로 회원가입 할 수 없습니다.")
                }
            } catch (e: Exception) {
                Log.e("SIGNUP_FLOW", "signUp() 예외 발생 ${e.message}", e)
                _message.emit("알 수 없는 오류로 회원가입 할 수 없습니다.")
            }
        }
    }
}
