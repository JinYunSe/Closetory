package com.ssafy.closetory.authActivity.logout

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.homeActivity.mypage.MyPageRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class MyPageViewModel : ViewModel() {

    private val repository = MyPageRepository()

    // 이벤트는 SharedFlow
    private val _logoutSuccess = MutableSharedFlow<Boolean>(replay = 0)
    val logoutSuccess: SharedFlow<Boolean> = _logoutSuccess

    private val _message = MutableSharedFlow<String>(replay = 0)
    val message: SharedFlow<String> = _message

    fun logout() {
        viewModelScope.launch {
            try {
                val res = repository.logout()

                Log.d("DEBUG", "################")
                Log.d("LOGOUT_FLOW", "response body = ${res.body()}")
                Log.d("DEBUG", "################")

                if (res.isSuccessful) {
                    val body = res.body()
                    _logoutSuccess.emit(true)
                    _message.emit(body?.responseMessage!!)
                } else {
                    val body = res.body()
                    _logoutSuccess.emit(false)
                    _message.emit(body?.errorMessage!!)
                }
            } catch (e: Exception) {
                Log.e("LOGOUT_FLOW", "logout() 예외 발생 ${e.message}", e)
                _logoutSuccess.emit(false)
                _message.emit("로그아웃 예외사항 발생")
            }
        }
    }
}
