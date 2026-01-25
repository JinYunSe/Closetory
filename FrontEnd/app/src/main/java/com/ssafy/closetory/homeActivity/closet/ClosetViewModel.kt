package com.ssafy.closetory.homeActivity.closet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.ClosetResponse
import kotlinx.coroutines.launch

private const val TAG = "ClosetViewModel_싸피"

class ClosetViewModel : ViewModel() {

    private val repository = ClosetRepository()

    private val _closetData = MutableLiveData<ClosetResponse?>()
    val closetData: LiveData<ClosetResponse?> = _closetData

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun getClothesList(tags: List<Int>?, color: String?, seasons: List<Int>?, onlyMine: Boolean?) {
        viewModelScope.launch {
            try {
                val res = repository.getClothesList(
                    tags,
                    color,
                    seasons,
                    onlyMine
                )

                if (res.isSuccessful) { // 통신 결과 200번 때 결과
                    val body = res.body()
                    val data = body?.data

                    Log.d(TAG, "getClothesList: $data")

                    _closetData.value = data
                } else { // 통신 결과 400, 500번 때 결과
                    val body = res.body()
                    _errorMessage.value = body?.errorMessage!!
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "네트워크 오류"
            }
        }
    }
}
