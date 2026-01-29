package com.ssafy.closetory.homeActivity.mypage.edit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.EditProfileInfoResponse
import com.ssafy.closetory.dto.EditProfilePasswordRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

private const val TAG = "EditProfileViewModel_싸피"

class EditProfileViewModel : ViewModel() {

    // 네트워크 호출 담당 Repository 초기화임
    private val repository = EditProfileRepository()

    // 회원정보 조회 결과 전달임
    private val _userProfile = MutableSharedFlow<EditProfileInfoResponse>(extraBufferCapacity = 1)
    val userProfile = _userProfile.asSharedFlow()

    // 회원정보 수정 성공 결과 전달임
    private val _updateResult = MutableSharedFlow<Unit?>(extraBufferCapacity = 1)
    val updateResult = _updateResult.asSharedFlow()

    // 토스트 메시지 전달임
    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val message = _message.asSharedFlow()

    // 로딩 상태 전달임
    private val _isLoading = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val isLoading = _isLoading.asSharedFlow()

    // 회원정보 조회 요청 처리임
    fun loadUserProfile(userId: Int) {
        viewModelScope.launch {
            _isLoading.tryEmit(true)

            try {
                val res = repository.getUserProfile(userId)

                if (res.isSuccessful) {
                    val body = res.body()
                    val data = body?.data

                    if (data != null) {
                        _userProfile.tryEmit(data)
                    } else {
                        _message.tryEmit(body?.errorMessage ?: "회원정보를 불러오지 못했습니다.")
                    }
                } else {
                    val apiError = parseErrorBody(res)
                    _message.tryEmit(apiError?.errorMessage ?: "요청 실패 (code=${res.code()})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadUserProfile exception", e)
                _message.tryEmit(e.message ?: "네트워크 오류")
            } finally {
                _isLoading.tryEmit(false)
            }
        }
    }

    // 회원정보 수정 멀티파트 요청 처리임
    fun updateProfileMultipart(profilePhoto: MultipartBody.Part?, bodyPhoto: MultipartBody.Part?, data: RequestBody) {
        viewModelScope.launch {
            _isLoading.tryEmit(true)

            try {
                val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID)
                    ?: run {
                        _message.tryEmit("로그인이 필요합니다.")
                        return@launch
                    }

                val res = repository.updateProfileMultipart(
                    userId = userId,
                    profilePhoto = profilePhoto,
                    bodyPhoto = bodyPhoto,
                    data = data
                )

                if (res.isSuccessful) {
                    val body = res.body()
                    _updateResult.tryEmit(Unit)

                    val msg = body?.responseMessage
                        ?: body?.errorMessage
                        ?: "응답 메시지가 없습니다."
                    _message.tryEmit(msg)
                } else {
                    val apiError = parseErrorBody(res)
                    val msg = apiError?.errorMessage ?: "요청 실패 (code=${res.code()})"
                    _message.tryEmit(msg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateProfileMultipart exception", e)
                _message.tryEmit(e.message ?: "네트워크 오류")
            } finally {
                _isLoading.tryEmit(false)
            }
        }
    }

    // 비밀번호 변경 요청 처리임
    fun changePassword(newPassword: String, newPasswordConfirm: String) {
        viewModelScope.launch {
            _isLoading.tryEmit(true)

            try {
                val userId = ApplicationClass.sharedPreferences.getUserId(ApplicationClass.USERID)
                    ?: run {
                        _message.tryEmit("로그인이 필요합니다.")
                        return@launch
                    }

                val res = repository.changePassword(
                    userId = userId,
                    request = EditProfilePasswordRequest(newPassword, newPasswordConfirm)
                )

                if (res.isSuccessful) {
                    val body = res.body()
                    val msg = body?.responseMessage
                        ?: body?.errorMessage
                        ?: "응답 메시지가 없습니다."
                    _message.tryEmit(msg)
                } else {
                    val apiError = parseErrorBody(res)
                    _message.tryEmit(apiError?.errorMessage ?: "요청 실패 (code=${res.code()})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "changePassword exception", e)
                _message.tryEmit(e.message ?: "네트워크 오류")
            } finally {
                _isLoading.tryEmit(false)
            }
        }
    }

    // errorBody ApiResponse 파싱 처리임
    private fun <T> parseErrorBody(res: Response<ApiResponse<T>>): ApiResponse<T>? {
        return try {
            val json = res.errorBody()?.string() ?: return null
            val type = object : TypeToken<ApiResponse<T>>() {}.type
            Gson().fromJson<ApiResponse<T>>(json, type)
        } catch (_: Exception) {
            null
        }
    }
}
