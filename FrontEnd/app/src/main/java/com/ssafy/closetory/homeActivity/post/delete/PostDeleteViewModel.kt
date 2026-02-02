package com.ssafy.closetory.homeActivity.post.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.homeActivity.post.PostRepository
import com.ssafy.closetory.homeActivity.post.PostService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

private const val TAG = "PostDeleteViewModel_싸피"

class PostDeleteViewModel : ViewModel() {

    private val repository = PostRepository()

    sealed class UiEvent {
        data class DeleteSuccess(val postId: Int) : UiEvent()
        data class DeleteFail(val message: String) : UiEvent()
    }

    private val _event = MutableSharedFlow<UiEvent>()
    val event: SharedFlow<UiEvent> = _event

    fun deletePost(postId: Int) {
        viewModelScope.launch {
            val result = repository.deletePost(postId)

            if (result.errorMessage == null) {
                _event.emit(UiEvent.DeleteSuccess(postId))
            } else {
                _event.emit(UiEvent.DeleteFail(result.errorMessage ?: "삭제 실패"))
            }
        }
    }
}
