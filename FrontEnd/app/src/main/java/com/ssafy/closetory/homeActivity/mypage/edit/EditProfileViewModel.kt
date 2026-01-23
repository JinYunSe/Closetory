package com.ssafy.closetory.homeActivity.mypage.edit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.EditProfileInfoResponse
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {

    private val repository = EditProfileRepository()

    private val _userProfile = MutableLiveData<EditProfileInfoResponse>()
    val userProfile: LiveData<EditProfileInfoResponse> = _userProfile

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun loadUserProfile(userId: Int) {
        Log.d("loadUserProfile launch전전전", "loadUserProfile launch전")

        viewModelScope.launch {
            Log.d("EDIT_PROFILE", "API call start, userId=$userId")
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
}
