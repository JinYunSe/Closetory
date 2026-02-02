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

    fun loadPostDetail(postId: Int) {
        Log.d(TAG, "ViewModel loadPostDetail() 진입")

        viewModelScope.launch {
            try {
                val apiRes = repository.getPostDetail(postId)

                if (apiRes.httpStatusCode in 200..299 && apiRes.data != null) {
                    _postDetail.value = apiRes.data

                    Log.d(TAG, "loadPostDetail 조회 성공 : ${_postDetail}")
                } else {
                    _message.emit(apiRes.errorMessage ?: "게시글 상세 조회 실패")
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadPostDetail 예외: ${e.message}", e)
                _message.emit("네트워크 오류")
            }
        }
    }

    fun toggleClothesSave(postId: Int, clothesId: Int, willSave: Boolean) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "toggleClothesSave: postId=$postId, clothesId=$clothesId, willSave=$willSave")

                val res = if (willSave) {
                    repository.postClothesRental(clothesId)
                } else {
                    repository.deleteClothesRental(clothesId)
                }

                if (res.isSuccessful) {
                    Log.d(TAG, "toggleClothesSave 성공")
                    val msg = res.body()?.responseMessage ?: if (willSave) "저장 완료" else "저장 해제 완료"
                    _message.emit(msg)
                    loadPostDetail(postId)
                } else {
                    val errBody = runCatching { res.errorBody()?.string() }.getOrNull()
                    Log.e(TAG, "toggleClothesSave 실패: code=${res.code()}, errorBody=$errBody")
                    _message.emit("요청 실패(code=${res.code()})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "toggleClothesSave 예외: ${e.message}", e)
                _message.emit("네트워크 오류")
            }
        }
    }
}
