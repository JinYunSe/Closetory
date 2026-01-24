package com.ssafy.closetory.homeActivity.registrationCloth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.RegistrationClothRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class RegistrationClothViewModel : ViewModel() {

    private val repository = RegistrationClothRepository()

    private val _message = MutableSharedFlow<String>(replay = 0)
    val message: SharedFlow<String?> = _message

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
}
