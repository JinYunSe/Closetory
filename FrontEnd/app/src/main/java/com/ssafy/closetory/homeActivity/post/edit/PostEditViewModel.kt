package com.ssafy.closetory.homeActivity.post.edit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.closetory.dto.PostDetailResponse
import com.ssafy.closetory.dto.PostEditRequest
import com.ssafy.closetory.dto.PostEditResponse
import com.ssafy.closetory.homeActivity.post.PostRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

private const val TAG = "PostEditViewModel_싸피"

class PostEditViewModel : ViewModel() {

    private var loadedPostId: Int? = null

    private val repository = PostRepository()

    private val _postDetail = MutableStateFlow<PostDetailResponse?>(null)
    val postDetail = _postDetail.asStateFlow()

    private val _editResult = MutableSharedFlow<PostEditResponse?>(extraBufferCapacity = 1)
    val editResult = _editResult.asSharedFlow()

    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val message = _message.asSharedFlow()

    // 수정 화면 진입 시 기존 게시글 상세 조회
    fun loadPostDetail(postId: Int) {
        // 네트워크 재호출 방지
        if (loadedPostId == postId && postDetail.value != null) return
        loadedPostId = postId

        viewModelScope.launch {
            try {
                val res = repository.getPostDetail(postId)
                if (res.data != null) {
                    _postDetail.value = res.data
                }
                val msg = res.responseMessage ?: res.errorMessage ?: "응답 메시지가 없습니다."
                _message.tryEmit(msg)
            } catch (e: Exception) {
                Log.e(TAG, "loadPostDetail exception", e)
                _message.tryEmit(e.message ?: "네트워크 오류")
            }
        }
    }

    fun editPost(
        postId: Int,
        photo: MultipartBody.Part?, // 사진 미변경이면 null
        title: String,
        content: String,
        items: List<Int>
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "editPost() postId=$postId title=$title content=$content items=$items photo=$photo")

                // 전체 필드 전송
                val res = repository.editPost(
                    postId = postId,
                    photo = photo,
                    request = PostEditRequest(
                        title = title,
                        content = content,
                        items = items
                    )
                )

                // repository가 ApiResponse로 내려주므로 여기서는 성공/실패를 data로 판단
                if (res.data != null) {
                    _editResult.tryEmit(res.data)
                }

                val msg = res.responseMessage ?: res.errorMessage ?: "응답 메시지가 없습니다."
                _message.tryEmit(msg)
            } catch (e: Exception) {
                Log.e(TAG, "editPost exception", e)
                _message.tryEmit(e.message ?: "네트워크 오류")
            }
        }
    }
}
