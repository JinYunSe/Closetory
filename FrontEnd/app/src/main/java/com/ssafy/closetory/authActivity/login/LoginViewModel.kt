// LoginViewModel.kt

package com.ssafy.closetory.authActivity.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.LoginRequest
import com.ssafy.closetory.dto.TokenResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

private const val TAG = "LoginViewModel_싸피"

class LoginViewModel : ViewModel() {

    private val repository = LoginRepository()

    private val _loginData = MutableSharedFlow<TokenResponse>(replay = 0)
    val loginData: SharedFlow<TokenResponse> = _loginData.asSharedFlow()

    private val _message = MutableSharedFlow<String>(replay = 0)
    val message: SharedFlow<String> = _message.asSharedFlow()

    fun login(username: String, password: String) {
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
                        _loginData.emit(tokenData)
                        _message.emit(body.responseMessage ?: "로그인 성공")
                    } else {
                        _message.emit(body?.responseMessage ?: "로그인 응답이 비어있습니다.")
                    }
                } else {
                    _message.emit(
                        body?.errorMessage ?: "로그인 실패"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "login() 예외 발생: ${e.message}", e)
            }
        }
    }
}
