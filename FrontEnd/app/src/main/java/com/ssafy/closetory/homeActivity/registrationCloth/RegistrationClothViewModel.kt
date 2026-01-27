package com.ssafy.closetory.homeActivity.registrationCloth

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.OriginalImageRequest
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

    private val _maskedImage = MutableLiveData<String>()
    val maskedImage: LiveData<String> = _maskedImage

    fun registrationCloth(photoUrl: Uri, tags: List<Int>, clothesTypes: Int, seasons: List<Int>, color: String) {
        viewModelScope.launch {
            try {
                val res = repository.registrationCloth(
                    RegistrationClothRequest(
                        photoUrl,
                        tags,
                        clothesTypes,
                        seasons,
                        color
                    )
                )

                Log.d(TAG, "옷 등록 통신 결과 ${res.body()?.data!!}")

                if (res.isSuccessful) {
                    val body = res.body()!!
                    _message.emit(body.responseMessage!!)
                } else {
                    val body = res.body()!!
                    _message.emit(body.errorMessage!!)
                }
            } catch (e: Exception) {
                _message.emit(e.message ?: "네트워크 오류")
            }
        }
    }

    fun removeImageBackground(binary: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "보낼 바이너리 사진 $binary")

                val res = repository.removeImageBackground(
                    binary
                )

                if (res.isSuccessful) {
                    val data = res.body()!!.data!!
                    _maskedImage.value = data.maskedImage
                    Log.d(TAG, "통신 받은 결과 ${_maskedImage.value}")
                } else {
                    val errorMessage = res.body()!!.errorMessage!!
                    _message.emit(errorMessage)
                    Log.d(TAG, "통신 받은 결과 실패 $errorMessage")
                }
            } catch (e: Exception) {
                Log.d(TAG, "removeImageBackground: ${e.message}")
                _message.emit("네트워크 오류 발생")
            }
        }
    }
}
