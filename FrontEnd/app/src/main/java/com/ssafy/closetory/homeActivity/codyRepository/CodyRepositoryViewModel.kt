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

    fun getLooks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            Log.d(TAG, "룩 목록 조회 시작")

            repository.getLooks()
                .onSuccess { data ->
                    Log.d(TAG, "룩 조회 성공 - ${data.size}개")
                    _looks.value = data
                }
                .onFailure { exception ->
                    Log.e(TAG, "룩 조회 실패", exception)
                    _error.value = exception.message ?: "데이터를 불러오는데 실패했습니다"
                }

            _isLoading.value = false
        }
    }

    fun clearErrorMessage() {
        _error.value = null
    }
}
