// LogoutViewModel

package com.ssafy.closetory.authActivity.logout

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class LogoutViewModel : ViewModel() {

    private val repository = LogoutRepository()

    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> = _logoutSuccess

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun logout(accessToken: String) {
        viewModelScope.launch {
            try {
                val res = repository.logout(accessToken)
                val errorJson = res.errorBody()?.string()
                // ===== 디버깅 로그 =====
                Log.d("DEBUG", "################")
                Log.d("LOGOUT_FLOW", "HTTP code = ${res.code()}")
                Log.d("LOGOUT_FLOW", "response body = ${res.body()}")
                Log.d("LOGOUT_FLOW", "error body = $errorJson")
                Log.d("DEBUG", "################")
                // =====================
                if (res.isSuccessful) {
                    _logoutSuccess.value = true
                    _message.value = "로그아웃에 성공했습니다."
                } else {
                    // errorMessage 파싱 (있으면 로그로만 확인)
                    val errorMessage = try {
                        if (errorJson.isNullOrBlank()) {
                            null
                        } else {
                            JSONObject(errorJson).optString("errorMessage", null)
                        }
                    } catch (e: Exception) {
                        null
                    }

                    Log.e(
                        "LOGOUT_FLOW",
                        "로그아웃 실패 - message=${errorMessage ?: "unknown"}"
                    )

                    _logoutSuccess.value = false
                    _message.value =
                        errorMessage ?: "알 수 없는 이유로 실패"
                }
            } catch (e: Exception) {
                // 네트워크 / 파싱 / 기타 예외
                Log.e("LOGOUT_FLOW", "logout() 예외 발생 ${e.message}", e)
                _logoutSuccess.value = false
                _message.value = "로그아웃 예외사항 발생"
            }
        }
    }
}
