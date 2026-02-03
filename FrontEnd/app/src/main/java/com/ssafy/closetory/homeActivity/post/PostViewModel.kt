package com.ssafy.closetory.homeActivity.post

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.PostCreateRequest
import com.ssafy.closetory.dto.PostCreateResponse
import com.ssafy.closetory.dto.PostDetailResponse
import com.ssafy.closetory.dto.PostEditRequest
import com.ssafy.closetory.dto.PostEditResponse
import com.ssafy.closetory.dto.PostItemResponse
import com.ssafy.closetory.dto.PostQueryFilter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Response

private const val TAG = "PostViewModel_싸피"

class PostViewModel : ViewModel() {

    // =========================================================
    // Dependencies
    // =========================================================
    private val repository = PostRepository()

    // =========================================================
    // Public state/event (exposed)
    // =========================================================
    // message
    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val message: SharedFlow<String> = _message.asSharedFlow()

    // list
    private val _postList = MutableLiveData<List<PostItemResponse>>(emptyList())
    val postList: LiveData<List<PostItemResponse>> = _postList

    // detail
    private val _postDetail = MutableStateFlow<PostDetailResponse?>(null)
    val postDetail: StateFlow<PostDetailResponse?> = _postDetail.asStateFlow()

    // create result
    private val _createResult = MutableSharedFlow<PostCreateResponse?>(extraBufferCapacity = 1)
    val createResult: SharedFlow<PostCreateResponse?> = _createResult.asSharedFlow()

    // edit result
    private val _editResult = MutableSharedFlow<PostEditResponse?>(extraBufferCapacity = 1)
    val editResult: SharedFlow<PostEditResponse?> = _editResult.asSharedFlow()

    // delete event
    sealed class DeleteEvent {
        data class Success(val postId: Int) : DeleteEvent()
        data class Fail(val message: String) : DeleteEvent()
    }

    private val _deleteEvent = MutableSharedFlow<DeleteEvent>(extraBufferCapacity = 1)
    val deleteEvent: SharedFlow<DeleteEvent> = _deleteEvent.asSharedFlow()

    // =========================================================
    // Internal variables
    // =========================================================
    private var loadedDetailPostId: Int? = null

    // =========================================================
    // List (검색 + 필터 / 필터만)
    // =========================================================

    /**
     * 검색 + 필터
     * keyword가 null/blank면 getPostsFilter를 타도록 분기
     */
    fun loadPosts(keyword: String?, filter: PostQueryFilter) {
        viewModelScope.launch {
            try {
                val kw = keyword?.trim().takeIf { !it.isNullOrEmpty() }

                val apiRes = if (kw == null) {
                    repository.getPostsFilter(filter)
                } else {
                    repository.getPosts(keyword = kw, filter = filter)
                }

                Log.d(TAG, "loadPosts 응답: $apiRes")

                val data = apiRes.data
                if (data != null) {
                    _postList.value = data
                } else {
                    _postList.value = emptyList()
                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "게시글을 불러오지 못했습니다.")
                }
            } catch (e: Exception) {
                _postList.value = emptyList()
                _message.tryEmit(e.message ?: "네트워크 오류")
            }
        }
    }

    /**
     * 필터만 조회
     */
    fun loadPostsFilter(filter: PostQueryFilter) {
        viewModelScope.launch {
            try {
                val apiRes = repository.getPostsFilter(filter)
                Log.d(TAG, "loadPostsFilter 응답: $apiRes")

                val data = apiRes.data
                if (data != null) {
                    _postList.value = data
                } else {
                    _postList.value = emptyList()
                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "게시글을 불러오지 못했습니다.")
                }
            } catch (e: Exception) {
                _postList.value = emptyList()
                _message.tryEmit(e.message ?: "네트워크 오류")
            }
        }
    }

    // =========================================================
    // Detail
    // =========================================================
    fun loadPostDetail(postId: Int, force: Boolean = false) {
        if (!force && loadedDetailPostId == postId && _postDetail.value != null) return
        loadedDetailPostId = postId

        viewModelScope.launch {
            try {
                val apiRes = repository.getPostDetail(postId)

                if (apiRes.httpStatusCode in 200..299 && apiRes.data != null) {
                    _postDetail.value = apiRes.data
                } else {
                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "게시글 상세 조회 실패")
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadPostDetail 예외", e)
                _message.tryEmit(e.message ?: "네트워크 오류")
            }
        }
    }

    // =========================================================
    // Create
    // =========================================================
    fun createPost(photo: MultipartBody.Part, title: String, content: String, items: List<Int>) {
        viewModelScope.launch {
            try {
                val res = repository.createPost(
                    photo = photo,
                    request = PostCreateRequest(title = title, content = content, items = items)
                )

                if (res.isSuccessful) {
                    val body = res.body()
                    _createResult.tryEmit(body?.data)
                    _message.tryEmit(body?.responseMessage ?: body?.errorMessage ?: "등록 완료")
                } else {
                    val apiError = parseErrorBody<PostCreateResponse>(res)
                    _message.tryEmit(apiError?.errorMessage ?: "등록 실패(code=${res.code()})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "createPost 예외", e)
                _message.tryEmit(e.message ?: "네트워크 오류")
            }
        }
    }

    // =========================================================
    // Update
    // =========================================================
    fun editPost(postId: Int, photo: MultipartBody.Part?, title: String, content: String, items: List<Int>) {
        viewModelScope.launch {
            try {
                val apiRes = repository.editPost(
                    postId = postId,
                    photo = photo,
                    request = PostEditRequest(title = title, content = content, items = items)
                )

                if (apiRes.data != null) _editResult.tryEmit(apiRes.data)
                _message.tryEmit(apiRes.responseMessage ?: apiRes.errorMessage ?: "수정 완료")

                if (apiRes.httpStatusCode in 200..299) {
                    loadPostDetail(postId, force = true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "editPost 예외", e)
                _message.tryEmit(e.message ?: "네트워크 오류")
            }
        }
    }

    // =========================================================
    // Delete
    // =========================================================
    fun deletePost(postId: Int) {
        viewModelScope.launch {
            try {
                val apiRes = repository.deletePost(postId)
                val ok = apiRes.httpStatusCode in 200..299

                if (ok) {
                    _deleteEvent.tryEmit(DeleteEvent.Success(postId))
                    _message.tryEmit(apiRes.responseMessage ?: "삭제 완료")
                } else {
                    val msg = apiRes.errorMessage ?: apiRes.responseMessage ?: "삭제 실패"
                    _deleteEvent.tryEmit(DeleteEvent.Fail(msg))
                    _message.tryEmit(msg)
                }
            } catch (e: Exception) {
                val msg = e.message ?: "네트워크 오류"
                _deleteEvent.tryEmit(DeleteEvent.Fail(msg))
                _message.tryEmit(msg)
            }
        }
    }

    // =========================================================
    // (선택) 옷 저장/해제
    // =========================================================
    fun toggleClothesSave(postId: Int, clothesId: Int, willSave: Boolean) {
        viewModelScope.launch {
            try {
                val res = if (willSave) {
                    repository.postClothesRental(clothesId)
                } else {
                    repository.deleteClothesRental(clothesId)
                }

                if (res.isSuccessful) {
                    val msg =
                        res.body()?.responseMessage ?: if (willSave) "저장 완료" else "저장 해제 완료"
                    _message.tryEmit(msg)
                    loadPostDetail(postId, force = true)
                } else {
                    _message.tryEmit("요청 실패(code=${res.code()})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "toggleClothesSave 예외", e)
                _message.tryEmit(e.message ?: "네트워크 오류")
            }
        }
    }

    // =========================================================
    // Utils
    // =========================================================
    private inline fun <reified T> parseErrorBody(res: Response<ApiResponse<T>>): ApiResponse<T>? {
        return try {
            val json = res.errorBody()?.string() ?: return null
            val type = object : TypeToken<ApiResponse<T>>() {}.type
            Gson().fromJson<ApiResponse<T>>(json, type)
        } catch (_: Exception) {
            null
        }
    }
}
