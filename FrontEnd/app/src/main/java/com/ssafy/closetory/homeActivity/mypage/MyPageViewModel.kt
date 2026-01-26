package com.ssafy.closetory.homeActivity.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.ApplicationClass
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

private const val TAG = "MyPageViewModel_싸피"
class MyPageViewModel : ViewModel() {

    private val repository = MyPageRepository()

    private val _passwordVerified = MutableSharedFlow<Boolean>()
    val passwordVerified = _passwordVerified.asSharedFlow()

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

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
}
