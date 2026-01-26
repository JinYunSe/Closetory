// MyPageViewModel

package com.ssafy.closetory.homeActivity.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.EditProfileInfoResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

private const val TAG = "MyPageViewModel_싸피"

class MyPageViewModel : ViewModel() {

    // 마이페이지 Repository
    private val repository = MyPageRepository()

    // 회원정보 데이터 (화면에 유지되는 데이터)
    private val _userProfile = MutableSharedFlow<EditProfileInfoResponse>()
    val userProfile: SharedFlow<EditProfileInfoResponse> = _userProfile

    // 비밀번호 검증 결과 Flow
    private val _passwordVerified = MutableSharedFlow<Boolean>()
    val passwordVerified = _passwordVerified.asSharedFlow()

    // 로그아웃 성공 여부 Flow
    private val _logoutSuccess = MutableSharedFlow<Boolean>(replay = 0)
    val logoutSuccess: SharedFlow<Boolean> = _logoutSuccess

    // 사용자에게 보여줄 메시지 Flow
    private val _message = MutableSharedFlow<String>(replay = 0)
    val message: SharedFlow<String> = _message

    // ------------------- 요청 처리 -------------------
    // 회원정보 조회 요청 처리
    fun loadUserProfile(userId: Int) {
        Log.d(TAG, "loadUserProfile: ViewModel_loadUserProfile 실행")
        viewModelScope.launch {
            try {
                val res = repository.getUserProfile(userId)

                Log.d(TAG, "getUserProfile code=${res.code()} body=${res.body()} err=${res.errorBody()?.string()}")

                if (res.isSuccessful) {
                    val body = res.body()
                    val data = body?.data

                    if (data != null) {
                        _userProfile.emit(data)
                    } else {
                        _message.emit("회원정보를 불러오지 못했습니다.")
                    }
                } else {
                    val body = res.body()
                    _message.emit(body?.errorMessage ?: "회원정보 조회 실패")
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadUserProfile error", e)
                _message.emit("네트워크 오류")
            }
        }
    }

    // 비밀번호 검증 요청 처리
    fun checkPassword(password: String) {
        // SharedPreferences 에 저장된 사용자 ID 조회
        val userId =
            ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID)
                ?: return

        Log.d(TAG, "checkPassword: userId : $userId")

        // 비동기 처리를 위한 코루틴 실행
        viewModelScope.launch {
            // 비밀번호 검증 API 요청
            val res = repository.checkPassword(userId, password)

            // 🔍 여기서 ApiResponse 전부 확인 가능
            Log.d(TAG, "httpStatus: ${res.httpStatusCode}")
            Log.d(TAG, "responseMessage: ${res.responseMessage}")
            Log.d(TAG, "errorMessage: ${res.errorMessage}")
            Log.d(TAG, "data: ${res.data}")

            // HTTP 상태 코드 기준으로 결과 분기 처리
            if (res.httpStatusCode == 200) {
                // 비밀번호 검증 성공 이벤트 전달
                _passwordVerified.emit(true)
                _message.emit(res.responseMessage ?: "확인되었습니다.")
            } else {
                // 비밀번호 검증 실패 이벤트 전달
                _passwordVerified.emit(false)
                _message.emit(res.errorMessage ?: "비밀번호가 올바르지 않습니다.")
            }
        }
    }

    // 로그아웃 요청 처리
    fun logout() {
        viewModelScope.launch {
            try {
                // 로그아웃 API 요청
                val res = repository.logout()

                // 디버그용 응답 로그 출력
                Log.d("DEBUG", "################")
                Log.d("LOGOUT_FLOW", "response body = ${res.body()}")
                Log.d("DEBUG", "################")

                if (res.isSuccessful) {
                    // 로그아웃 성공 처리
                    val body = res.body()

                    _logoutSuccess.emit(true)
                    _message.emit(body?.responseMessage!!)

                    // 토큰 및 사용자 ID 로컬 데이터 삭제
                    ApplicationClass.authManager.clearToken()
                    ApplicationClass.sharedPreferences.clearUserId(ApplicationClass.USERID)
                } else {
                    // 로그아웃 실패 처리
                    val body = res.body()

                    _logoutSuccess.emit(false)
                    _message.emit(body?.errorMessage!!)
                }
            } catch (e: Exception) {
                // 네트워크 또는 예외 상황 처리
                Log.e("LOGOUT_FLOW", "logout() 예외 발생 ${e.message}", e)

                _logoutSuccess.emit(false)
                _message.emit("로그아웃 예외사항 발생")
            }
        }
    }
}
