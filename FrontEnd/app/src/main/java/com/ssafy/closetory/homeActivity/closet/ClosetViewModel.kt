package com.ssafy.closetory.homeActivity.closet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.ClosetDataDto
import kotlinx.coroutines.launch
import retrofit2.Response

private const val TAG = "ClosetViewModel_싸피"
class ClosetViewModel : ViewModel() {

    private val repository = ClosetRepository()

    private val _closetData = MutableLiveData<ClosetDataDto?>()
    val closetData: LiveData<ClosetDataDto?> = _closetData

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun getClothesList(
        tags: List<String>?,
        color: String?,
        seasons: List<String>?,
        onlyLike: Boolean?,
        onlyMine: Boolean?
    ) {
        viewModelScope.launch {
            try {
                val res = repository.getClothesList(
                    tags,
                    color,
                    seasons,
                    onlyLike,
                    onlyMine
                )

                Log.d(TAG, "getClothesList: $res")

                if (res.isSuccessful) {
                    val body = res.body()

                    val data = body?.data
                    if (data != null) {
                        _closetData.value = data
                    } else {
                        _message.value = body?.responseMessage
                    }
                } else {
                    _message.value = res.message()
                }
            } catch (e: Exception) {
                _message.value = e.message ?: "네트워크 오류"
            }
        }
    }
}
