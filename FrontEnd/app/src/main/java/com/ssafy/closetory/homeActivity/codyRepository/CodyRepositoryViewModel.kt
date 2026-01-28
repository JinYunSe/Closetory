package com.ssafy.closetory.homeActivity.codyRepository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.CodyRepositoryResponse
import kotlinx.coroutines.launch

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

    init {
        getLooks()
    }

    fun getLooks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getLooks()
                .onSuccess { data ->
                    _looks.value = data
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "데이터를 불러오는데 실패했습니다"
                }

            _isLoading.value = false
        }
    }
}
