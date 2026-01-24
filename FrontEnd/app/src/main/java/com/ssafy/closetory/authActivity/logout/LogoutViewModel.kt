// LogoutViewModel

package com.ssafy.closetory.authActivity.logout

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.ApplicationClass
import kotlinx.coroutines.launch

class LogoutViewModel : ViewModel() {

    private val repository = LogoutRepository()

    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> = _logoutSuccess

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun logout() {
        viewModelScope.launch {
            try {
                val res = repository.logout()

                Log.d("LOGOUT_FLOW", "HTTP code = ${res.code()}")
                Log.d("LOGOUT_FLOW", "body = ${res.body()}")

                val body = res.body()

                if (res.isSuccessful) {
                    _logoutSuccess.value = true
                    _message.value = body?.responseMessage ?: "로그아웃에 성공했습니다."
                } else {
                    _logoutSuccess.value = false
                    _message.value = body?.errorMessage ?: body?.responseMessage ?: "알 수 없는 이유로 실패"
                }
            } catch (e: Exception) {
                Log.e("LOGOUT_FLOW", "logout() 예외 발생 ${e.message}", e)
                _logoutSuccess.value = false
                _message.value = "로그아웃 예외사항 발생"
            }
        }
    }
}
