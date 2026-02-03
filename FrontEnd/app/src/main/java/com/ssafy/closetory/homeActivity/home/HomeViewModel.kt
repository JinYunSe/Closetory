package com.ssafy.closetory.homeActivity.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.StylingResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel_싸피"

class HomeViewModel : ViewModel() {

    private val _stylingList = MutableLiveData<List<StylingResponse>>(emptyList())
    val stylingList: LiveData<List<StylingResponse>> = _stylingList

    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val message: SharedFlow<String> = _message.asSharedFlow()

    private val homeRepository = HomeRepository()

    fun getStylingList(isMain: Boolean) {
        viewModelScope.launch {
            try {
                val res = homeRepository.getStylingList(isMain)

                if (res.isSuccessful) {
                    val list = res.body()?.data.orEmpty() // NPE 방지
                    _stylingList.value = list
                    Log.d(TAG, "홈 캘린더 옷 조회 성공 size=${list.size}")
                } else {
                    val msg =
                        res.errorBody()?.string()
                            ?: res.body()?.errorMessage
                            ?: "요청 실패 (${res.code()})"
                    Log.d(TAG, "홈 캘린더 옷 조회 실패 : $msg")
                    _message.tryEmit(msg)
                }
            } catch (e: Exception) {
                val msg = e.message ?: "네트워크 오류 발생"
                Log.e(TAG, "홈 룩 목록 조회 예외 : $msg", e)
                _message.tryEmit(msg)
            }
        }
    }
}
