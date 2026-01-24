// LoginViewModel

package com.ssafy.closetory.authActivity.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.LoginRequest
import com.ssafy.closetory.dto.TokenResponse
import kotlinx.coroutines.launch

private const val TAG = "LoginViewModel_싸피"
class LoginViewModel : ViewModel() {

    private val repository = LoginRepository()

    private val _loginData = MutableLiveData<TokenResponse?>()
    val loginData: LiveData<TokenResponse?> = _loginData

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _message.value = "필수 항목을 입력해주세요."
            return
        }

        viewModelScope.launch {
            try {
                val request = LoginRequest(username, password)
                val res = repository.login(request)

                Log.d(TAG, "HTTP code = ${res.code()}")
                Log.d(TAG, "body = ${res.body()}")

                val body = res.body()

                if (res.isSuccessful) {
                    val tokenData = body?.data

                    if (tokenData != null) {
                        _loginData.value = tokenData
                        _message.value = body.responseMessage ?: "로그인 성공"
                    } else {
                        _message.value = body?.responseMessage ?: "로그인 응답이 비어있습니다."
                    }
                } else {
                    // 서버가 실패
                    _message.value = body?.errorMessage ?: body?.responseMessage ?: "알 수 없는 오류로 로그인 할 수 없습니다."
                }
            } catch (e: Exception) {
                Log.e(TAG, "login() 예외 발생: ${e.message}", e)
                _message.value = "알 수 없는 오류로 로그인 할 수 없습니다."
            }
        }
    }
}
