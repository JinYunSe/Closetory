// LoginViewModel

package com.ssafy.closetory.authActivity.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.LoginRequest
import com.ssafy.closetory.dto.LoginResponse
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repository = LoginRepository()

    private val _loginData = MutableLiveData<LoginResponse?>()
    val loginData: LiveData<LoginResponse?> = _loginData

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun login(username: String, password: String) {
        Log.d("LOGIN_FLOW", "ViewModel login() 진입")

        viewModelScope.launch {
            Log.d("LOGIN_FLOW", "coroutine 시작")

            try {
                val request = LoginRequest(username, password)
                // request 로그 확인
                Log.d("LOGIN_REQUEST", request.toString())

                val res = repository.login(request)
                val errorJson = res.errorBody()?.string()
//              디버깅 목록
                Log.d("DEBUG", "################")
                // [추가] HTTP 상태 코드
                Log.d("LOGIN_FLOW", "HTTP code = ${res.code()}")
                // [추가] 성공 body
                Log.d("LOGIN_FLOW", "response body = ${res.body()}")
                // [추가] 에러 body (Raw JSON)
                Log.d("LOGIN_FLOW", "error body = $errorJson")
                Log.d("DEBUG", "################")
//              디버깅 목록

                if (res.isSuccessful) {
                    val body = res.body()

                    if (body?.data != null) {
                        _loginData.value = body.data
                    } else {
                        _message.value =
                            body?.responseMessage ?: "로그인 응답이 비어있습니다"
                    }
                } else {
                    // ✨ [변경] errorBody(JSON) → errorMessage만 파싱
                    val errorMessage = try {
                        if (errorJson.isNullOrBlank()) {
                            null
                        } else {
                            val jsonObject = org.json.JSONObject(errorJson)
                            jsonObject.optString("errorMessage", null)
                        }
                    } catch (e: Exception) {
                        null
                    }
                    // ✨ [변경] 기본 메시지 처리
                    _message.value =
                        errorMessage ?: "알 수 없는 오류로 로그인 할 수 없습니다."
                }
            } catch (e: Exception) {
                // ✨ [추가] 실제 원인을 로그로 확인
                Log.e("LOGIN_FLOW", "login() 예외 발생", e)

                // ✨ [변경] 사용자에게는 포괄 메시지
                _message.value = "알 수 없는 오류로 로그인 할 수 없습니다."
            }
        }
    }
}
