package com.ssafy.closetory.homeActivity.mypage.edit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.dto.EditProfilePasswordRequest
import com.ssafy.closetory.dto.EditProfileUpdateRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

private const val TAG = "EditProfileViewModel_싸피"

class EditProfileViewModel : ViewModel() {

    private val repository = EditProfileRepository()

    // 회원정보 데이터 (화면에 유지되는 데이터)
    private val _userProfile = MutableSharedFlow<EditProfileInfoResponse>()
    val userProfile: SharedFlow<EditProfileInfoResponse> = _userProfile

    // 단발성 메시지
    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message

    // 회원정보 조회
    fun loadUserProfile(userId: Int) {
        Log.d(TAG, "loadUserProfile: ViewModel_loadUserProfile 실행")
        viewModelScope.launch {
            try {
                val res = repository.getUserProfile(userId)

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

    // 회원정보 수정
    fun updateProfile(
        nickname: String,
        height: Int,
        weight: Int,
        gender: String,
        alarmEnabled: Boolean,
        profilePhotoUrl: String?,
        bodyPhotoUrl: String?
    ) {
        viewModelScope.launch {
            try {
                val request = EditProfileUpdateRequest(
                    nickname = nickname,
                    height = height,
                    weight = weight,
                    gender = gender,
                    alarmEnabled = alarmEnabled,
                    profilePhotoUrl = profilePhotoUrl,
                    bodyPhotoUrl = bodyPhotoUrl

                )

                val res = repository.updateProfile(
                    userId = 0, // 토큰 기반이면 의미 없음
                    request = request
                )

                if (res.isSuccessful) {
                    val body = res.body()
                    _message.emit(body?.responseMessage ?: "회원정보가 수정되었습니다.")
                } else {
                    val body = res.body()
                    _message.emit(body?.errorMessage ?: "회원정보 수정 실패")
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateProfile error", e)
                _message.emit("네트워크 오류")
            }
        }
    }

    // 비밀번호 변경
    fun changePassword(newPassword: String, newPasswordConfirm: String) {
        viewModelScope.launch {
            try {
                val request = EditProfilePasswordRequest(
                    newPassword = newPassword,
                    newPasswordConfirm = newPasswordConfirm
                )

                // userId 검증
                val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID) ?: run {
                    _message.emit("로그인이 필요합니다.")
                    return@launch
                }

                val res = repository.changePassword(
                    userId = userId,
                    request = request
                )
                Log.d(TAG, "changePassword: $res")
                if (res.isSuccessful) {
                    val body = res.body()
                    _message.emit(body?.responseMessage ?: "비밀번호가 변경되었습니다.")
                } else {
                    val body = res.body()
                    _message.emit(body?.errorMessage ?: "비밀번호 변경 실패")
                }
            } catch (e: Exception) {
                Log.e(TAG, "changePassword error", e)
                _message.emit("네트워크 오류")
            }
        }
    }
}
