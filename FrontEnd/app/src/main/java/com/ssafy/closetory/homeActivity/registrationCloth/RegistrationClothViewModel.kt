package com.ssafy.closetory.homeActivity.registrationCloth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RegistrationClothViewModel : ViewModel() {

    private val repository = RegistrationClothRepository()

    private val _message = MutableLiveData<String>()
    val message: LiveData<String?> = _message

    fun registrationCloth(photoUrl: String, tags: List<Int>, clothesTypes: String, seasons: List<Int>, color: String) {
        viewModelScope.launch {
            try {
                val res = repository.registrationCloth(
                    photoUrl,
                    tags,
                    clothesTypes,
                    seasons,
                    color
                )

                if (photoUrl.isBlank()) {
                }

                if (res.isSuccessful) {
                    val body = res.body()!!

                    _message.value = body.responseMessage!!
                } else {
                    val body = res.body()!!

                    _message.value = body.errorMessage!!
                }
            } catch (e: Exception) {
                _message.value = e.message ?: "네트워크 오류"
            }
        }
    }
}
