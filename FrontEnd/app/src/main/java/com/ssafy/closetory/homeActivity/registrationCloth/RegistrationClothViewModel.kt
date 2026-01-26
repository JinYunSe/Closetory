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
                val res = repository.removeImageBackground(
                    OriginalImageRequest(binary)
                )

                if (res.isSuccessful) {
                    val data = res.body()!!.data!!
                    _maskedImage.value = data.maskedImage
                } else {
                    val errorMessage = res.body()!!.errorMessage!!
                    _message.emit(errorMessage)
                }
            } catch (e: Exception) {
                Log.d(TAG, "removeImageBackground: ${e.message}")
                _message.emit("네트워크 오류 발생")
            }
        }
    }
}
