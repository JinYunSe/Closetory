package com.ssafy.closetory.homeActivity.mypage.edit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.dto.EditProfilePasswordRequest
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {

    private val repository = EditProfileRepository()

    // 회원정보 조회 전용 LiveData 추가
    private val _userProfile = MutableLiveData<EditProfileInfoResponse>()
    val userProfile: LiveData<EditProfileInfoResponse> = _userProfile

    // 비밀번호 변경용 LiveData 추가
    private val _passwordChangeSuccess = MutableLiveData<Boolean>()
    val passwordChangeSuccess: LiveData<Boolean> = _passwordChangeSuccess

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    // 회원정보 조회 전용 함수
    fun loadUserProfile(userId: Int) {
        viewModelScope.launch {
            val response = repository.getUserProfile(userId)

            if (response.isSuccessful) {
                Log.d("EDIT_PROFILE", "API body=${response.body()}")
                response.body()?.let {
                    _userProfile.value = it
                }
            } else {
                Log.d("EDIT_PROFILE", "API error=${response.errorBody()?.string()}")
                _message.value = "회원정보를 불러오지 못했습니다."
            }
        }
    }

    // 비밀번호 변경 전용 함수
    fun changePassword(userId: Int, currentPassword: String, newPassword: String, newPasswordConfirm: String) {
        // 🔹 프론트에서 할 수 있는 최소 검증
        if (currentPassword.isBlank() ||
            newPassword.isBlank() ||
            newPasswordConfirm.isBlank()
        ) {
            _message.value = "모든 항목을 입력해주세요."
            return
        }

        if (newPassword != newPasswordConfirm) {
            _message.value = "새 비밀번호가 일치하지 않습니다."
            return
        }
        if (newPassword.length < 8) {
            _message.value = "비밀번호는 8자리 이상이어야 합니다."
            return
        }

        viewModelScope.launch {
            val response = repository.changePassword(
                userId = userId,
                request = EditProfilePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    newPasswordConfirm = newPasswordConfirm
                )
            )

            if (response.isSuccessful) {
                // ✅ 성공
                _passwordChangeSuccess.value = true
                _message.value =
                    response.body()?.responseMessage ?: "비밀번호 변경 완료"
            } else {
                // ❌ 실패
                _message.value =
                    response.errorBody()?.string() ?: "비밀번호 변경에 실패했습니다."
            }
        }
    }

    // 비밀번호 변경 전
}
