package com.ssafy.closetory.homeActivity.tagOnboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.TagResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "TagOnboardingViewModel_싸피"

class TagOnboardingViewModel(private val repository: TagOnboardingRepository = TagOnboardingRepository()) :
    ViewModel() {

    private val _tagOptions = MutableStateFlow<List<TagOption>>(emptyList())
    val tagOptions: StateFlow<List<TagOption>> = _tagOptions.asStateFlow()

    private val _tagLoadFailMessage = MutableSharedFlow<String>(replay = 0)
    val tagLoadFailMessage = _tagLoadFailMessage.asSharedFlow()

    private val _submitSuccess = MutableSharedFlow<Unit>(replay = 0)
    val submitSuccess = _submitSuccess.asSharedFlow()

    private val _submitFailMessage = MutableSharedFlow<String>(replay = 0)
    val submitFailMessage = _submitFailMessage.asSharedFlow()

    // 서버에서 태그 목록을 가져와서 온보딩 화면에 사용할 형태(TagOption)로 변환
    fun loadTags() {
        viewModelScope.launch {
            runCatching {
                repository.getTagsList()
            }.onSuccess { res ->
                if (res.isSuccessful) {
                    val data: List<TagResponse> = res.body()?.data.orEmpty()

                    _tagOptions.value = data
                        .sortedBy { it.tagId }
                        .map { TagOption(id = it.tagId, name = it.tagName) }
                } else {
                    _tagLoadFailMessage.emit("태그 목록 요청 실패 (${res.code()})")
                }
            }.onFailure { e ->
                _tagLoadFailMessage.emit(e.message ?: "태그 목록 네트워크 오류")
            }
        }
    }

    fun postTagOnboarding(userId: Int, tags: List<Int>) {
        viewModelScope.launch {
            runCatching {
                repository.postTagOnboarding(userId, tags)
            }.onSuccess { res ->
                if (res.isSuccessful) {
                    // ✅ message.value (X) → emit (O)
                    _submitSuccess.emit(Unit)
                } else {
                    _submitFailMessage.emit("서버 요청 실패 (${res.code()})")
                }
            }.onFailure { e ->
                _submitFailMessage.emit(e.message ?: "네트워크 오류")
            }
        }
    }
}
