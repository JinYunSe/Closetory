package com.ssafy.closetory.homeActivity.codyRepository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.CodyRepositoryResponse
import kotlinx.coroutines.launch

private const val TAG = "CodyRepositoryViewModel"

class CodyRepositoryViewModel(
    private val repository: CodyRepository = CodyRepository(
        ApplicationClass.retrofit.create(CodyRepositoryService::class.java)
    )
) : ViewModel() {

    private val _looks = MutableLiveData<List<CodyRepositoryResponse>>()
    val looks: LiveData<List<CodyRepositoryResponse>> = _looks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    /**
     * 룩 목록 조회
     */
    fun getLooks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            Log.d(TAG, "========================================")
            Log.d(TAG, "🔍 룩 목록 조회 시작")
            Log.d(TAG, "========================================")

            repository.getLooks()
                .onSuccess { data ->
                    Log.d(TAG, "✅ 룩 조회 성공 - 총 ${data.size}개")
                    Log.d(TAG, "========================================")

                    // ✅ 각 아이템의 날짜 상세 로그
                    data.forEachIndexed { index, item ->
                        Log.d(TAG, "[$index] 룩 정보:")
                        Log.d(TAG, "  - lookId: ${item.lookId}")
                        Log.d(
                            TAG,
                            "  - date: ${if (item.date.isNullOrBlank()) "❌ null/빈값 (미등록)" else "✅ '${item.date}'"}"
                        )
                        Log.d(TAG, "  - photoUrl: ${item.photoUrl}")
                        Log.d(TAG, "  - aiReason: ${item.aiReason ?: "없음"}")
                        Log.d(TAG, "  - onlyMine: ${item.onlyMine}")
                        Log.d(TAG, "  ---")
                    }

                    // 날짜 통계
                    val withDate = data.count { !it.date.isNullOrBlank() }
                    val withoutDate = data.count { it.date.isNullOrBlank() }
                    Log.d(TAG, "========================================")
                    Log.d(TAG, "📊 날짜 통계:")
                    Log.d(TAG, "  - 날짜 있음: $withDate 개")
                    Log.d(TAG, "  - 날짜 없음: $withoutDate 개 (캘린더 미등록)")
                    Log.d(TAG, "========================================")

                    _looks.value = data
                }
                .onFailure { exception ->
                    Log.e(TAG, "========================================")
                    Log.e(TAG, "❌ 룩 조회 실패", exception)
                    Log.e(TAG, "========================================")
                    _error.value = exception.message ?: "데이터를 불러오는데 실패했습니다"
                }

            _isLoading.value = false
        }
    }

    /**
     * 캘린더에 룩 등록
     */
    fun registerToCalendar(lookId: Int, date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            Log.d(TAG, "🗓️ 캘린더 등록 시작 - lookId: $lookId, date: $date")

            repository.registerToCalendar(lookId, date)
                .onSuccess {
                    Log.d(TAG, "✅ 캘린더 등록 성공")
                    _successMessage.value = "캘린더에 등록되었습니다"

                    // ✅ 등록 후 목록 다시 불러오기
                    getLooks()
                }
                .onFailure { exception ->
                    Log.e(TAG, "❌ 캘린더 등록 실패", exception)
                    _error.value = exception.message ?: "캘린더 등록에 실패했습니다"
                }

            _isLoading.value = false
        }
    }

    /**
     * 룩 삭제
     */
    fun deleteLook(lookId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            Log.d(TAG, "🗑️ 룩 삭제 시작 - lookId: $lookId")

            repository.deleteLook(lookId)
                .onSuccess {
                    Log.d(TAG, "✅ 룩 삭제 성공")
                    _successMessage.value = "룩이 삭제되었습니다"

                    // 삭제 후 목록 새로고침
                    getLooks()
                }
                .onFailure { exception ->
                    Log.e(TAG, "❌ 룩 삭제 실패", exception)
                    _error.value = exception.message ?: "룩 삭제에 실패했습니다"
                }

            _isLoading.value = false
        }
    }

    fun clearErrorMessage() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
