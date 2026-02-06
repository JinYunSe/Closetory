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

    // 2) 날짜별 상/하의 색(캘린더 칠하기용): "yyyy-MM-dd" -> (topColorName, bottomColorName)
    private val _dayColorMap = MutableLiveData<Map<String, Pair<String?, String?>>>(emptyMap())
    val dayColorMap: LiveData<Map<String, Pair<String?, String?>>> = _dayColorMap

    // 3) 이미 등록된 날짜 Set (중복 등록/선택 막기용)
    private val _registeredDateSet = MutableLiveData<Set<String>>(emptySet())
    val registeredDateSet: LiveData<Set<String>> = _registeredDateSet

    // 4) lookId -> date (저장소에서 날짜 오버레이 표시용)
    private val _lookIdToDateMap = MutableLiveData<Map<Int, String>>(emptyMap())
    val lookIdToDateMap: LiveData<Map<Int, String>> = _lookIdToDateMap

    private val homeRepository = HomeRepository()

    fun getStylingList(isMain: Boolean) {
        viewModelScope.launch {
            try {
                val res = homeRepository.getStylingList(isMain)

                if (res.isSuccessful) {
                    val list = res.body()?.data!!
                    _stylingList.value = list
                    Log.d(TAG, "홈 캘린더 옷 조회 성공 : $list")
                } else {
                    val errorMessage = res.body()?.errorMessage
                    Log.d(TAG, "홈 캘린더 옷 조회 실패 : $errorMessage")
                    _message.emit(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "홈 룩 목록 조회 : ${e.message}")
            }
        }
    }
}
