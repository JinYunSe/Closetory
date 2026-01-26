package com.ssafy.closetory.homeActivity.mypage
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.ApplicationClass
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

private const val TAG = "MyPageViewModel_싸피"

class MyPageViewModel : ViewModel() {

    private val repository = MyPageRepository()

    private val _passwordVerified = MutableSharedFlow<Boolean>()
    val passwordVerified = _passwordVerified.asSharedFlow()

    // 이벤트는 SharedFlow
    private val _logoutSuccess = MutableSharedFlow<Boolean>(replay = 0)
    val logoutSuccess: SharedFlow<Boolean> = _logoutSuccess

    private val _message = MutableSharedFlow<String>(replay = 0)
    val message: SharedFlow<String> = _message

    // 비밀번호 검증 요청
    fun checkPassword(password: String) {
        val userId =
            ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID)
                ?: return

        Log.d(TAG, "checkPassword: userId : $userId")

        viewModelScope.launch {
            val res = repository.checkPassword(userId, password)

            if (res.httpStatusCode == 200) {
                _passwordVerified.emit(true)
                _message.emit(res.responseMessage ?: "확인되었습니다.")
            } else {
                _passwordVerified.emit(false)
                _message.emit(res.errorMessage ?: "비밀번호가 올바르지 않습니다.")
            }
        }
    }

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
                    ApplicationClass.authManager.clearToken()
                    ApplicationClass.sharedPreferences.clearUserId(ApplicationClass.USERID)
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
