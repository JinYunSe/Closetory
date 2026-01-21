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
        weightText: String
    ) {
        Log.d("SIGNUP_FLOW", "ViewModel signUp() 진입")

        // 1. 필수값 검증
        if (username.isBlank() ||
            password.isBlank() ||
            passwordConfirm.isBlank() ||
            nickname.isBlank()
        ) {
            _message.value = "필수 항목을 모두 입력해주세요."
            return
        }

        // 2. 비밀번호 검증
        if (password.length < 8) {
            _message.value = "비밀번호는 8자 이상이어야 합니다."
            return
        }

        if (password != passwordConfirm) {
            _message.value = "비밀번호가 일치하지 않습니다."
            return
        }

        // 3. 키 / 몸무게 숫자 변환
        val height = heightText.toIntOrNull()
        val weight = weightText.toIntOrNull()

        if (height == null || weight == null) {
            _message.value = "키와 몸무게는 숫자로 입력해주세요."
            return
        }
        // 4. 성별 선택 여부
        if (gender == null) {
            _message.value = "성별을 선택해주세요."
            return
        }

        viewModelScope.launch {
            try {
                val request = SignUpRequest(
                    username = username,
                    password = password,
                    nickname = nickname,
                    gender = gender,
                    height = height,
                    weight = weight
                )
                // request 로그 확인
                Log.d("SIGNUP_REQUEST", request.toString())

                val res = repository.signUp(request)

                val errorJson = res.errorBody()?.string()

                Log.d("DEBUG", "################")
                Log.d("SIGNUP_FLOW", "HTTP code = ${res.code()}")
                Log.d("SIGNUP_FLOW", "response body = ${res.body()}")
                Log.d("SIGNUP_FLOW", "error body = $errorJson")
                Log.d("DEBUG", "################")

                if (res.isSuccessful) {
                    _signUpSuccess.value = true
                    _message.value = "회원가입 성공"
                } else {
                    val errorMessage = try {
                        if (errorJson.isNullOrBlank()) {
                            null
                        } else {
                            org.json.JSONObject(errorJson)
                                .optString("errorMessage", null)
                        }
                    } catch (e: Exception) {
                        null
                    }

                    _message.value =
                        errorMessage ?: "알 수 없는 오류로 회원가입 할 수 없습니다."
                }
            } catch (e: Exception) {
                Log.e("SIGNUP_FLOW", "signUp() 예외 발생", e)
                _message.value = "알 수 없는 오류로 회원가입 할 수 없습니다."
            }
        }
    }
}
