package com.ssafy.closetory.homeActivity.post.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.PostDetailResponse
import com.ssafy.closetory.homeActivity.post.PostRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

private const val TAG = "PostDetailViewModel_싸피"

class PostDetailViewModel : ViewModel() {

    private val repository = PostRepository()

    private val _postDetail = MutableStateFlow<PostDetailResponse?>(null)
    val postDetail: StateFlow<PostDetailResponse?> = _postDetail

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    // 게시글 상세 조회
    fun loadPostDetail(postId: Int) {
        Log.d(TAG, "ViewModel loadPostDetail() 진입")

        viewModelScope.launch {
            Log.d(TAG, "coroutine 시작")

            try {
                val apiRes = repository.getPostDetail(postId)

                Log.d("DEBUG", "################")
                Log.d(TAG, "httpStatusCode = ${apiRes.httpStatusCode}")
                Log.d(TAG, "responseMessage = ${apiRes.responseMessage}")
                Log.d(TAG, "errorMessage = ${apiRes.errorMessage}")
                Log.d(TAG, "data = ${apiRes.data}")
                Log.d("DEBUG", "################")

                if (apiRes.httpStatusCode in 200..299 && apiRes.data != null) {
                    _postDetail.value = apiRes.data
                } else {
                    _message.emit(apiRes.errorMessage ?: "게시글 상세 조회 실패")
                }
            } catch (e: Exception) {
                Log.e(TAG, "예외 발생: ${e.message}", e)
                _message.emit("네트워크 오류")
            }
        }
    }
}
