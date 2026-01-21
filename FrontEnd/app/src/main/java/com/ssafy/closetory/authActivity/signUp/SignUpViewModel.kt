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

    fun signUp(id: String, password: String, passwordConfirm: String, nickname: String) {
        Log.d("SIGNUP_FLOW", "ViewModel signUp() 진입")

        // ✅ 1. 필수값 검증
        if (id.isBlank() ||
            password.isBlank() ||
            passwordConfirm.isBlank() ||
            nickname.isBlank()
        ) {
            _message.value = "모든 필수 항목을 입력해주세요."
            return
        }

        // ✅ 2. 비밀번호 길이
        if (password.length < 8) {
            _message.value = "비밀번호는 8자 이상이어야 합니다."
            return
        }

        // ✅ 3. 비밀번호 확인
        if (password != passwordConfirm) {
            _message.value = "비밀번호가 일치하지 않습니다."
            return
        }

        viewModelScope.launch {
            try {
                val res = repository.signUp(
                    SignUpRequest(
                        loginId = id,
                        password = password,
                        nickname = nickname
                    )
                )

                val errorJson = res.errorBody()?.string()

                // 🔍 Login과 동일한 디버깅 로그
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
