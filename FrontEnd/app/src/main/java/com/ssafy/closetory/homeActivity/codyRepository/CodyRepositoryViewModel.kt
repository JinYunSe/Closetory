package com.ssafy.closetory.homeActivity.codyRepository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.CodyRepositoryResponse
import kotlinx.coroutines.launch

class CodyRepositoryViewModel(private val repository: CodyRepository) : ViewModel() {

    // 변경가능한 데이터 / 외부접근차단 / ViewModel사용하는 데이터 / 내부용
    private val _looks = MutableLiveData<List<CodyRepositoryResponse>>()

    // 외부용으로 외부적으로 보이는 데이터(읽기 전용)
    val looks: LiveData<List<CodyRepositoryResponse>> = _looks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // viewModel이 생성되는 순간 실행되는것
    init {
        loadLooks()
    }

    fun loadLooks() {
        // scope로 생명주기 생성
        viewModelScope.launch {
            // 화면에 로딩 UI 표시
            _isLoading.value = true
            // 이전 에러 메시지 초기화
            _error.value = null

            repository.getLooks()
                // 서버에서 받은 룩 리스트 저장, _looks 값 변경, looks.observe {} 하고 있던 Fragment 자동 호출, RecyclerView 자동 갱신
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
