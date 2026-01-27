// RegistrationClothViewModel.kt
package com.ssafy.closetory.homeActivity.registrationCloth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.RegistrationClothRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

private const val TAG = "RegistrationClothViewModel_싸피"

class RegistrationClothViewModel : ViewModel() {

    private val repository = RegistrationClothRepository()

    private val _message = MutableSharedFlow<String>(replay = 0)
    val message: SharedFlow<String?> = _message

    private val _maskedImage = MutableLiveData<String?>()
    val maskedImage: LiveData<String?> = _maskedImage

    fun clearMaskedUrl() {
        _maskedImage.value = null
    }

    fun removeImageBackground(binary: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "send binary part=$binary")
                val res = repository.removeImageBackground(binary)

                if (res.isSuccessful) {
                    val data = res.body()!!.data!!
                    _maskedImage.value = data.maskedImage
                    Log.d(TAG, "masked url=${_maskedImage.value}")
                } else {
                    _message.emit(res.body()?.errorMessage ?: "마스킹 실패")
                }
            } catch (e: Exception) {
                Log.d(TAG, "removeImageBackground: ${e.message}")
                _message.emit("네트워크 오류 발생")
            }
        }
    }

    // 등록은 "마스킹된 이미지 URL"로 진행
    fun registrationCloth(photoUrl: String, tags: List<Int>, clothesTypes: Int, seasons: List<Int>, color: String) {
        viewModelScope.launch {
            try {
                val res = repository.registrationCloth(
                    RegistrationClothRequest(
                        photoUrl = photoUrl,
                        tags = tags,
                        clothesTypes = clothesTypes,
                        seasons = seasons,
                        color = color
                    )
                )

                if (res.isSuccessful) {
                    _message.emit(res.body()?.responseMessage ?: "등록 성공")
                } else {
                    _message.emit(res.body()?.errorMessage ?: "등록 실패")
                }
            } catch (e: Exception) {
                _message.emit(e.message ?: "네트워크 오류")
            }
        }
    }
}
