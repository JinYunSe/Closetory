package com.ssafy.closetory.homeActivity.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.StylingResponse
import kotlin.math.log
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

    fun getStylingList(isMain: Boolean) {
        viewModelScope.launch {
            try {
                val res = homeRepository.getStylingList(isMain)

                if (res.isSuccessful) {
                    val list = res.body()?.data!!
                    _stylingList.value = list
                } else {
                    val errorMessage = res.body()?.errorMessage
                    _message.emit(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "홈 룩 목록 조회 : ${e.message}")
                _message.emit(e.message ?: "네트워크 오류 발생")
            }
        }
    }
}
