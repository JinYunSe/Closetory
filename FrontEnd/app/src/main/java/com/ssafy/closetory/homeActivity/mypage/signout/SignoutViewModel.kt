package com.ssafy.closetory.homeActivity.mypage.signout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

// 회원 탈퇴 ViewModel
class SignoutViewModel : ViewModel() {

    private val repository = SignoutRepository()

    // 회원 탈퇴 성공 이벤트 (1회성)
    private val _signoutSuccess = MutableSharedFlow<Unit>()
    val signoutSuccess: SharedFlow<Unit> = _signoutSuccess

    // 에러 메시지 이벤트 (1회성)
    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message

    // 회원 탈퇴 요청
    fun signout(userId: Int, password: String) {
        viewModelScope.launch {
            try {
                val res = repository.signout(userId, password)

                if (res.isSuccessful) {
                    // HTTP 200~299
                    _signoutSuccess.emit(Unit)
                } else {
                    // HTTP 400, 500
                    val body = res.body()
                    _message.emit(
                        body?.errorMessage ?: "회원 탈퇴에 실패했습니다."
                    )
                }
            } catch (e: Exception) {
            }
        }
    }
}
