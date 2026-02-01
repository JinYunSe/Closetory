package com.ssafy.closetory.homeActivity.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.StylingResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel_싸피"
class HomeViewModel : ViewModel() {

    // 월별 스타일링 리스트
    private val _stylingList = MutableLiveData<List<StylingResponse>>()
    val stylingList: LiveData<List<StylingResponse>> = _stylingList

    private val _message = MutableSharedFlow<String?>(replay = 0)
    val message: SharedFlow<String?> = _message

    private val homeRepository = HomeRepository()

    fun getStylingList(month: Int) {
        viewModelScope.launch {
            try {
                val res = homeRepository.getStylingList()
            } catch (e: Exception) {
                Log.d(TAG, "3개월 동안 코드 내역 : ${e.message ?: "네트워크 오류"}")
            }
        }
    }
}
