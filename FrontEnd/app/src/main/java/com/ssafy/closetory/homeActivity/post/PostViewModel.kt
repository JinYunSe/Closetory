// package com.ssafy.closetory.homeActivity.post
//
// import android.util.Log
// import androidx.lifecycle.LiveData
// import androidx.lifecycle.MutableLiveData
// import androidx.lifecycle.ViewModel
// import androidx.lifecycle.viewModelScope
// import com.google.gson.Gson
// import com.google.gson.reflect.TypeToken
// import com.ssafy.closetory.dto.*
// import kotlinx.coroutines.flow.*
// import kotlinx.coroutines.launch
// import okhttp3.MultipartBody
// import retrofit2.Response
//
// private const val TAG = "PostViewModel_싸피"
//
// class PostViewModel : ViewModel() {
//
//    // -------------------------
//    // Dependencies
//    // -------------------------
//    private val repository = PostRepository()
//
//    // -------------------------
//    // Public: message
//    // -------------------------
//    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
//    val message: SharedFlow<String> = _message.asSharedFlow()
//
//    // -------------------------
//    // Public: list (LiveData)
//    // -------------------------
//    private val _postList = MutableLiveData<List<PostItemResponse>>(emptyList())
//    val postList: LiveData<List<PostItemResponse>> = _postList
//
//    // -------------------------
//    // Public: detail (StateFlow)
//    // -------------------------
//    private val _postDetail = MutableStateFlow<PostDetailResponse?>(null)
//    val postDetail: StateFlow<PostDetailResponse?> = _postDetail.asStateFlow()
//
//    private var loadedDetailPostId: Int? = null
//
//    // -------------------------
//    // Public: create/edit/delete events
//    // -------------------------
//    private val _createResult = MutableSharedFlow<PostCreateResponse?>(extraBufferCapacity = 1)
//    val createResult: SharedFlow<PostCreateResponse?> = _createResult.asSharedFlow()
//
//    private val _editResult = MutableSharedFlow<PostEditResponse?>(extraBufferCapacity = 1)
//    val editResult: SharedFlow<PostEditResponse?> = _editResult.asSharedFlow()
//
//    sealed class DeleteEvent {
//        data class Success(val postId: Int) : DeleteEvent()
//        data class Fail(val message: String) : DeleteEvent()
//    }
//
//    private val _deleteEvent = MutableSharedFlow<DeleteEvent>(extraBufferCapacity = 1)
//    val deleteEvent: SharedFlow<DeleteEvent> = _deleteEvent.asSharedFlow()
//
//    // =========================================================
//    // List
//    // =========================================================
//    fun loadPosts(keyword: String?, filter: PostQueryFilter) {
//        viewModelScope.launch {
//            try {
//                val kw = keyword?.trim()!!
//                if (kw.isEmpty()) {
//                    _postList.value = emptyList()
//                    _message.tryEmit("검색어가 비어있습니다.")
//                    return@launch
//                }
//
//                val apiRes = repository.getPosts(keyword = kw, filter = filter)
//                val data = apiRes.data
//                if (data != null) {
//                    _postList.value = data!!
//                } else {
//                    _postList.value = emptyList()
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "게시글을 불러오지 못했습니다.")
//                }
//            } catch (e: Exception) {
//                _postList.value = emptyList()
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    fun loadPostsFilter(filter: PostQueryFilter) {
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.getPostsFilter(filter)
//                val data = apiRes.data
//                if (data != null) {
//                    _postList.value = data!!
//                } else {
//                    _postList.value = emptyList()
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "게시글을 불러오지 못했습니다.")
//                }
//            } catch (e: Exception) {
//                _postList.value = emptyList()
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Detail
//    // =========================================================
//    fun loadPostDetail(postId: Int, force: Boolean = false) {
//        if (!force && loadedDetailPostId == postId && _postDetail.value != null) return
//        loadedDetailPostId = postId
//
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.getPostDetail(postId)
//
//                if (apiRes.httpStatusCode in 200..299 && apiRes.data != null) {
//                    _postDetail.value = apiRes.data
//                } else {
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "게시글 상세 조회 실패")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "loadPostDetail 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Like (✅ 조회수 증가 버그 방지: 상세 재조회 금지)
//    // =========================================================
//    fun toggleLike(postId: Int) {
//        viewModelScope.launch {
//            val cur = _postDetail.value
//            if (cur == null || cur.postId != postId) return@launch
//
//            val willLike = !cur.isLiked
//
//            try {
//                val apiRes = if (willLike) repository.likePost(postId) else repository.unlikePost(postId)
//                val ok = apiRes.httpStatusCode in 200..299
//
//                if (!ok) {
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "좋아요 처리 실패")
//                    return@launch
//                }
//
//                // ✅ 서버 상세 재조회 안 함 (views 증가 방지)
//                val newCount = (cur.likeCount + if (willLike) 1 else -1).coerceAtLeast(0)
//                _postDetail.value = cur.copy(
//                    isLiked = willLike,
//                    likeCount = newCount
//                )
//            } catch (e: Exception) {
//                Log.e(TAG, "toggleLike 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Create
//    // =========================================================
//    fun createPost(photo: MultipartBody.Part, title: String, content: String, items: List<Int>) {
//        viewModelScope.launch {
//            try {
//                val res = repository.createPost(
//                    photo = photo,
//                    request = PostCreateRequest(title = title, content = content, items = items)
//                )
//
//                if (res.isSuccessful) {
//                    val body = res.body()
//                    _createResult.tryEmit(body?.data)
//                    _message.tryEmit(body?.responseMessage ?: body?.errorMessage ?: "등록 완료")
//                } else {
//                    val apiError = parseErrorBody<PostCreateResponse>(res)
//                    _message.tryEmit(apiError?.errorMessage ?: "등록 실패(code=${res.code()})")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "createPost 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Update
//    // =========================================================
//    fun editPost(postId: Int, photo: MultipartBody.Part?, title: String, content: String, items: List<Int>) {
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.editPost(
//                    postId = postId,
//                    photo = photo,
//                    request = PostEditRequest(title = title, content = content, items = items)
//                )
//
//                if (apiRes.data != null) _editResult.tryEmit(apiRes.data)
//                _message.tryEmit(apiRes.responseMessage ?: apiRes.errorMessage ?: "수정 완료")
//
//                // 수정 성공 시에만 상세 재조회 (이건 의도된 동작)
//                if (apiRes.httpStatusCode in 200..299) {
//                    loadPostDetail(postId, force = true)
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "editPost 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Delete
//    // =========================================================
//    fun deletePost(postId: Int) {
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.deletePost(postId)
//                val ok = apiRes.httpStatusCode in 200..299
//
//                if (ok) {
//                    _deleteEvent.tryEmit(DeleteEvent.Success(postId))
//                    _message.tryEmit(apiRes.responseMessage ?: "삭제 완료")
//                } else {
//                    val msg = apiRes.errorMessage ?: apiRes.responseMessage ?: "삭제 실패"
//                    _deleteEvent.tryEmit(DeleteEvent.Fail(msg))
//                    _message.tryEmit(msg)
//                }
//            } catch (e: Exception) {
//                val msg = e.message ?: "네트워크 오류"
//                _deleteEvent.tryEmit(DeleteEvent.Fail(msg))
//                _message.tryEmit(msg)
//            }
//        }
//    }
//
//    // =========================================================
//    // (선택) 옷 저장/해제
//    // =========================================================
//    fun toggleClothesSave(postId: Int, clothesId: Int, willSave: Boolean) {
//        viewModelScope.launch {
//            try {
//                val res = if (willSave) {
//                    repository.postClothesRental(clothesId)
//                } else {
//                    repository.deleteClothesRental(clothesId)
//                }
//
//                if (res.isSuccessful) {
//                    val msg = res.body()?.responseMessage ?: if (willSave) "저장 완료" else "저장 해제 완료"
//                    _message.tryEmit(msg)
//
//                    // 옷 저장 상태는 상세 items에 영향 → 상세 재조회 (서버가 views+1 하는 구조면 이 부분도 백엔드가 분리돼야 완벽)
//                    loadPostDetail(postId, force = true)
//                } else {
//                    _message.tryEmit("요청 실패(code=${res.code()})")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "toggleClothesSave 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    private inline fun <reified T> parseErrorBody(res: Response<ApiResponse<T>>): ApiResponse<T>? {
//        return try {
//            val json = res.errorBody()?.string() ?: return null
//            val type = object : TypeToken<ApiResponse<T>>() {}.type
//            Gson().fromJson<ApiResponse<T>>(json, type)
//        } catch (_: Exception) {
//            null
//        }
//    }
// }

// @file:Suppress("ktlint:standard:no-wildcard-imports")
//
// package com.ssafy.closetory.homeActivity.post
//
// import android.util.Log
// import androidx.lifecycle.LiveData
// import androidx.lifecycle.MutableLiveData
// import androidx.lifecycle.ViewModel
// import androidx.lifecycle.viewModelScope
// import com.google.gson.Gson
// import com.google.gson.reflect.TypeToken
// import com.ssafy.closetory.dto.*
// import kotlinx.coroutines.flow.*
// import kotlinx.coroutines.launch
// import okhttp3.MultipartBody
// import retrofit2.Response
//
// private const val TAG = "PostViewModel_싸피"
//
// class PostViewModel : ViewModel() {
//
//    // -------------------------
//    // Dependencies
//    // -------------------------
//    private val repository = PostRepository()
//
//    // -------------------------
//    // Public: message
//    // -------------------------
//    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
//    val message: SharedFlow<String> = _message.asSharedFlow()
//
//    // -------------------------
//    // Public: list (LiveData)
//    // -------------------------
//    private val _postList = MutableLiveData<List<PostItemResponse>>(emptyList())
//    val postList: LiveData<List<PostItemResponse>> = _postList
//
//    // -------------------------
//    // Public: detail (StateFlow)
//    // -------------------------
//    private val _postDetail = MutableStateFlow<PostDetailResponse?>(null)
//    val postDetail: StateFlow<PostDetailResponse?> = _postDetail.asStateFlow()
//
//    private var loadedDetailPostId: Int? = null
//
//    // -------------------------
//    // Public: comments (StateFlow) ✅ 추가
//    // -------------------------
//    private val _comments = MutableStateFlow<List<CommentDto>>(emptyList())
//    val comments: StateFlow<List<CommentDto>> = _comments.asStateFlow()
//
//    // -------------------------
//    // Public: create/edit/delete events
//    // -------------------------
//    private val _createResult = MutableSharedFlow<PostCreateResponse?>(extraBufferCapacity = 1)
//    val createResult: SharedFlow<PostCreateResponse?> = _createResult.asSharedFlow()
//
//    private val _editResult = MutableSharedFlow<PostEditResponse?>(extraBufferCapacity = 1)
//    val editResult: SharedFlow<PostEditResponse?> = _editResult.asSharedFlow()
//
//    sealed class DeleteEvent {
//        data class Success(val postId: Int) : DeleteEvent()
//        data class Fail(val message: String) : DeleteEvent()
//    }
//
//    private val _deleteEvent = MutableSharedFlow<DeleteEvent>(extraBufferCapacity = 1)
//    val deleteEvent: SharedFlow<DeleteEvent> = _deleteEvent.asSharedFlow()
//
//    // =========================================================
//    // List
//    // =========================================================
//    fun loadPosts(keyword: String?, filter: PostQueryFilter) {
//        viewModelScope.launch {
//            try {
//                val kw = keyword?.trim()!!
//                if (kw.isEmpty()) {
//                    _postList.value = emptyList()
//                    _message.tryEmit("검색어가 비어있습니다.")
//                    return@launch
//                }
//
//                val apiRes = repository.getPosts(keyword = kw, filter = filter)
//                val data = apiRes.data
//                if (data != null) {
//                    _postList.value = data!!
//                } else {
//                    _postList.value = emptyList()
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "게시글을 불러오지 못했습니다.")
//                }
//            } catch (e: Exception) {
//                _postList.value = emptyList()
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    fun loadPostsFilter(filter: PostQueryFilter) {
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.getPostsFilter(filter)
//                val data = apiRes.data
//                if (data != null) {
//                    _postList.value = data!!
//                } else {
//                    _postList.value = emptyList()
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "게시글을 불러오지 못했습니다.")
//                }
//            } catch (e: Exception) {
//                _postList.value = emptyList()
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Detail
//    // =========================================================
//    fun loadPostDetail(postId: Int, force: Boolean = false) {
//        if (!force && loadedDetailPostId == postId && _postDetail.value != null) return
//        loadedDetailPostId = postId
//
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.getPostDetail(postId)
//
//                if (apiRes.httpStatusCode in 200..299 && apiRes.data != null) {
//                    _postDetail.value = apiRes.data
//                } else {
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "게시글 상세 조회 실패")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "loadPostDetail 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Like (✅ 조회수 증가 버그 방지: 상세 재조회 금지)
//    // =========================================================
//    fun toggleLike(postId: Int) {
//        viewModelScope.launch {
//            val cur = _postDetail.value
//            if (cur == null || cur.postId != postId) return@launch
//
//            val willLike = !cur.isLiked
//
//            try {
//                val apiRes = if (willLike) repository.likePost(postId) else repository.unlikePost(postId)
//                val ok = apiRes.httpStatusCode in 200..299
//
//                if (!ok) {
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "좋아요 처리 실패")
//                    return@launch
//                }
//
//                // ✅ 서버 상세 재조회 안 함 (views 증가 방지)
//                val newCount = (cur.likeCount + if (willLike) 1 else -1).coerceAtLeast(0)
//                _postDetail.value = cur.copy(
//                    isLiked = willLike,
//                    likeCount = newCount
//                )
//            } catch (e: Exception) {
//                Log.e(TAG, "toggleLike 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Create
//    // =========================================================
//    fun createPost(photo: MultipartBody.Part, title: String, content: String, items: List<Int>) {
//        viewModelScope.launch {
//            try {
//                val res = repository.createPost(
//                    photo = photo,
//                    request = PostCreateRequest(title = title, content = content, items = items)
//                )
//
//                if (res.isSuccessful) {
//                    val body = res.body()
//                    _createResult.tryEmit(body?.data)
//                    _message.tryEmit(body?.responseMessage ?: body?.errorMessage ?: "등록 완료")
//                } else {
//                    val apiError = parseErrorBody<PostCreateResponse>(res)
//                    _message.tryEmit(apiError?.errorMessage ?: "등록 실패(code=${res.code()})")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "createPost 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Update
//    // =========================================================
//    fun editPost(postId: Int, photo: MultipartBody.Part?, title: String, content: String, items: List<Int>) {
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.editPost(
//                    postId = postId,
//                    photo = photo,
//                    request = PostEditRequest(title = title, content = content, items = items)
//                )
//
//                if (apiRes.data != null) _editResult.tryEmit(apiRes.data)
//                _message.tryEmit(apiRes.responseMessage ?: apiRes.errorMessage ?: "수정 완료")
//
//                // 수정 성공 시에만 상세 재조회 (이건 의도된 동작)
//                if (apiRes.httpStatusCode in 200..299) {
//                    loadPostDetail(postId, force = true)
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "editPost 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Delete
//    // =========================================================
//    fun deletePost(postId: Int) {
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.deletePost(postId)
//                val ok = apiRes.httpStatusCode in 200..299
//
//                if (ok) {
//                    _deleteEvent.tryEmit(DeleteEvent.Success(postId))
//                    _message.tryEmit(apiRes.responseMessage ?: "삭제 완료")
//                } else {
//                    val msg = apiRes.errorMessage ?: apiRes.responseMessage ?: "삭제 실패"
//                    _deleteEvent.tryEmit(DeleteEvent.Fail(msg))
//                    _message.tryEmit(msg)
//                }
//            } catch (e: Exception) {
//                val msg = e.message ?: "네트워크 오류"
//                _deleteEvent.tryEmit(DeleteEvent.Fail(msg))
//                _message.tryEmit(msg)
//            }
//        }
//    }
//
//    // =========================================================
//    // (선택) 옷 저장/해제
//    // =========================================================
//    fun toggleClothesSave(postId: Int, clothesId: Int, willSave: Boolean) {
//        viewModelScope.launch {
//            try {
//                val res = if (willSave) {
//                    repository.postClothesRental(clothesId)
//                } else {
//                    repository.deleteClothesRental(clothesId)
//                }
//
//                if (res.isSuccessful) {
//                    val msg = res.body()?.responseMessage ?: if (willSave) "저장 완료" else "저장 해제 완료"
//                    _message.tryEmit(msg)
//
//                    // 옷 저장 상태는 상세 items에 영향 → 상세 재조회 (서버가 views+1 하는 구조면 이 부분도 백엔드가 분리라야 완벽)
//                    loadPostDetail(postId, force = true)
//                } else {
//                    _message.tryEmit("요청 실패(code=${res.code()})")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "toggleClothesSave 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Comments (✅ 댓글 기능 추가)
//    // =========================================================
//
//    /**
//     * 댓글 목록 조회
//     */
//    fun loadComments(postId: Int) {
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.getComments(postId)
//
//                if (apiRes.httpStatusCode in 200..299 && apiRes.data != null) {
//                    _comments.value = apiRes.data // .comment 제거
//                    Log.d(TAG, "댓글 조회 성공: ${apiRes.data.size}개")
//                } else {
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "댓글을 불러올 수 없습니다.")
//                    _comments.value = emptyList()
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "loadComments 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//                _comments.value = emptyList()
//            }
//        }
//    }
//
//    /**
//     * 댓글 등록
//     */
//    fun createComment(postId: Int, content: String) {
//        if (content.isBlank()) {
//            viewModelScope.launch {
//                _message.tryEmit("댓글 내용을 입력하세요.")
//            }
//            return
//        }
//
//        viewModelScope.launch {
//            try {
//                val res = repository.createComment(postId, content)
//
//                if (res.isSuccessful) {
//                    val msg = res.body()?.responseMessage ?: "댓글이 등록되었습니다."
//                    _message.tryEmit(msg)
//
//                    // 댓글 등록 후 목록 다시 조회
//                    loadComments(postId)
//                } else {
//                    val errBody = runCatching { res.errorBody()?.string() }.getOrNull()
//                    Log.e(TAG, "댓글 등록 실패: code=${res.code()}, errorBody=$errBody")
//
//                    val errorMsg = when (res.code()) {
//                        400 -> "댓글 내용은 비워둘 수 없습니다."
//                        404 -> "존재하지 않는 게시글입니다."
//                        else -> "댓글 등록에 실패했습니다."
//                    }
//                    _message.tryEmit(errorMsg)
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "createComment 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Helper
//    // =========================================================
//    private inline fun <reified T> parseErrorBody(res: Response<ApiResponse<T>>): ApiResponse<T>? {
//        return try {
//            val json = res.errorBody()?.string() ?: return null
//            val type = object : TypeToken<ApiResponse<T>>() {}.type
//            Gson().fromJson<ApiResponse<T>>(json, type)
//        } catch (_: Exception) {
//            null
//        }
//    }
// }

// package com.ssafy.closetory.homeActivity.post
//
// import android.util.Log
// import androidx.lifecycle.LiveData
// import androidx.lifecycle.MutableLiveData
// import androidx.lifecycle.ViewModel
// import androidx.lifecycle.viewModelScope
// import com.google.gson.Gson
// import com.google.gson.reflect.TypeToken
// import com.ssafy.closetory.dto.*
// import kotlinx.coroutines.flow.*
// import kotlinx.coroutines.launch
// import okhttp3.MultipartBody
// import retrofit2.Response
//
// private const val TAG = "PostViewModel_싸피"
//
// class PostViewModel : ViewModel() {
//
//    // -------------------------
//    // Dependencies
//    // -------------------------
//    private val repository = PostRepository()
//
//    // -------------------------
//    // Public: message
//    // -------------------------
//    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
//    val message: SharedFlow<String> = _message.asSharedFlow()
//
//    // -------------------------
//    // Public: list (LiveData)
//    // -------------------------
//    private val _postList = MutableLiveData<List<PostItemResponse>>(emptyList())
//    val postList: LiveData<List<PostItemResponse>> = _postList
//
//    // -------------------------
//    // Public: detail (StateFlow)
//    // -------------------------
//    private val _postDetail = MutableStateFlow<PostDetailResponse?>(null)
//    val postDetail: StateFlow<PostDetailResponse?> = _postDetail.asStateFlow()
//
//    private var loadedDetailPostId: Int? = null
//
//    // -------------------------
//    // Public: create/edit/delete events
//    // -------------------------
//    private val _createResult = MutableSharedFlow<PostCreateResponse?>(extraBufferCapacity = 1)
//    val createResult: SharedFlow<PostCreateResponse?> = _createResult.asSharedFlow()
//
//    private val _editResult = MutableSharedFlow<PostEditResponse?>(extraBufferCapacity = 1)
//    val editResult: SharedFlow<PostEditResponse?> = _editResult.asSharedFlow()
//
//    sealed class DeleteEvent {
//        data class Success(val postId: Int) : DeleteEvent()
//        data class Fail(val message: String) : DeleteEvent()
//    }
//
//    private val _deleteEvent = MutableSharedFlow<DeleteEvent>(extraBufferCapacity = 1)
//    val deleteEvent: SharedFlow<DeleteEvent> = _deleteEvent.asSharedFlow()
//
//    // =========================================================
//    // List
//    // =========================================================
//    fun loadPosts(keyword: String?, filter: PostQueryFilter) {
//        viewModelScope.launch {
//            try {
//                val kw = keyword?.trim()!!
//                if (kw.isEmpty()) {
//                    _postList.value = emptyList()
//                    _message.tryEmit("검색어가 비어있습니다.")
//                    return@launch
//                }
//
//                val apiRes = repository.getPosts(keyword = kw, filter = filter)
//                val data = apiRes.data
//                if (data != null) {
//                    _postList.value = data!!
//                } else {
//                    _postList.value = emptyList()
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "게시글을 불러오지 못했습니다.")
//                }
//            } catch (e: Exception) {
//                _postList.value = emptyList()
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    fun loadPostsFilter(filter: PostQueryFilter) {
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.getPostsFilter(filter)
//                val data = apiRes.data
//                if (data != null) {
//                    _postList.value = data!!
//                } else {
//                    _postList.value = emptyList()
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "게시글을 불러오지 못했습니다.")
//                }
//            } catch (e: Exception) {
//                _postList.value = emptyList()
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Detail
//    // =========================================================
//    fun loadPostDetail(postId: Int, force: Boolean = false) {
//        if (!force && loadedDetailPostId == postId && _postDetail.value != null) return
//        loadedDetailPostId = postId
//
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.getPostDetail(postId)
//
//                if (apiRes.httpStatusCode in 200..299 && apiRes.data != null) {
//                    _postDetail.value = apiRes.data
//                } else {
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "게시글 상세 조회 실패")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "loadPostDetail 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Like (✅ 조회수 증가 버그 방지: 상세 재조회 금지)
//    // =========================================================
//    fun toggleLike(postId: Int) {
//        viewModelScope.launch {
//            val cur = _postDetail.value
//            if (cur == null || cur.postId != postId) return@launch
//
//            val willLike = !cur.isLiked
//
//            try {
//                val apiRes = if (willLike) repository.likePost(postId) else repository.unlikePost(postId)
//                val ok = apiRes.httpStatusCode in 200..299
//
//                if (!ok) {
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "좋아요 처리 실패")
//                    return@launch
//                }
//
//                // ✅ 서버 상세 재조회 안 함 (views 증가 방지)
//                val newCount = (cur.likeCount + if (willLike) 1 else -1).coerceAtLeast(0)
//                _postDetail.value = cur.copy(
//                    isLiked = willLike,
//                    likeCount = newCount
//                )
//            } catch (e: Exception) {
//                Log.e(TAG, "toggleLike 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Create
//    // =========================================================
//    fun createPost(photo: MultipartBody.Part, title: String, content: String, items: List<Int>) {
//        viewModelScope.launch {
//            try {
//                val res = repository.createPost(
//                    photo = photo,
//                    request = PostCreateRequest(title = title, content = content, items = items)
//                )
//
//                if (res.isSuccessful) {
//                    val body = res.body()
//                    _createResult.tryEmit(body?.data)
//                    _message.tryEmit(body?.responseMessage ?: body?.errorMessage ?: "등록 완료")
//                } else {
//                    val apiError = parseErrorBody<PostCreateResponse>(res)
//                    _message.tryEmit(apiError?.errorMessage ?: "등록 실패(code=${res.code()})")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "createPost 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Update
//    // =========================================================
//    fun editPost(postId: Int, photo: MultipartBody.Part?, title: String, content: String, items: List<Int>) {
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.editPost(
//                    postId = postId,
//                    photo = photo,
//                    request = PostEditRequest(title = title, content = content, items = items)
//                )
//
//                if (apiRes.data != null) _editResult.tryEmit(apiRes.data)
//                _message.tryEmit(apiRes.responseMessage ?: apiRes.errorMessage ?: "수정 완료")
//
//                // 수정 성공 시에만 상세 재조회 (이건 의도된 동작)
//                if (apiRes.httpStatusCode in 200..299) {
//                    loadPostDetail(postId, force = true)
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "editPost 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Delete
//    // =========================================================
//    fun deletePost(postId: Int) {
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.deletePost(postId)
//                val ok = apiRes.httpStatusCode in 200..299
//
//                if (ok) {
//                    _deleteEvent.tryEmit(DeleteEvent.Success(postId))
//                    _message.tryEmit(apiRes.responseMessage ?: "삭제 완료")
//                } else {
//                    val msg = apiRes.errorMessage ?: apiRes.responseMessage ?: "삭제 실패"
//                    _deleteEvent.tryEmit(DeleteEvent.Fail(msg))
//                    _message.tryEmit(msg)
//                }
//            } catch (e: Exception) {
//                val msg = e.message ?: "네트워크 오류"
//                _deleteEvent.tryEmit(DeleteEvent.Fail(msg))
//                _message.tryEmit(msg)
//            }
//        }
//    }
//
//    // =========================================================
//    // (선택) 옷 저장/해제
//    // =========================================================
//    fun toggleClothesSave(postId: Int, clothesId: Int, willSave: Boolean) {
//        viewModelScope.launch {
//            try {
//                val res = if (willSave) {
//                    repository.postClothesRental(clothesId)
//                } else {
//                    repository.deleteClothesRental(clothesId)
//                }
//
//                if (res.isSuccessful) {
//                    val msg = res.body()?.responseMessage ?: if (willSave) "저장 완료" else "저장 해제 완료"
//                    _message.tryEmit(msg)
//
//                    // 옷 저장 상태는 상세 items에 영향 → 상세 재조회 (서버가 views+1 하는 구조면 이 부분도 백엔드가 분리돼야 완벽)
//                    loadPostDetail(postId, force = true)
//                } else {
//                    _message.tryEmit("요청 실패(code=${res.code()})")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "toggleClothesSave 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    private inline fun <reified T> parseErrorBody(res: Response<ApiResponse<T>>): ApiResponse<T>? {
//        return try {
//            val json = res.errorBody()?.string() ?: return null
//            val type = object : TypeToken<ApiResponse<T>>() {}.type
//            Gson().fromJson<ApiResponse<T>>(json, type)
//        } catch (_: Exception) {
//            null
//        }
//    }
// }

// package com.ssafy.closetory.homeActivity.post
//
// import android.util.Log
// import androidx.lifecycle.LiveData
// import androidx.lifecycle.MutableLiveData
// import androidx.lifecycle.ViewModel
// import androidx.lifecycle.viewModelScope
// import com.google.gson.Gson
// import com.google.gson.reflect.TypeToken
// import com.ssafy.closetory.dto.*
// import kotlinx.coroutines.flow.*
// import kotlinx.coroutines.launch
// import okhttp3.MultipartBody
// import retrofit2.Response
//
// private const val TAG = "PostViewModel_싸피"
//
// class PostViewModel : ViewModel() {
//
//    // -------------------------
//    // Dependencies
//    // -------------------------
//    private val repository = PostRepository()
//
//    // -------------------------
//    // Public: message
//    // -------------------------
//    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
//    val message: SharedFlow<String> = _message.asSharedFlow()
//
//    // -------------------------
//    // Public: list (LiveData)
//    // -------------------------
//    private val _postList = MutableLiveData<List<PostItemResponse>>(emptyList())
//    val postList: LiveData<List<PostItemResponse>> = _postList
//
//    // -------------------------
//    // Public: detail (StateFlow)
//    // -------------------------
//    private val _postDetail = MutableStateFlow<PostDetailResponse?>(null)
//    val postDetail: StateFlow<PostDetailResponse?> = _postDetail.asStateFlow()
//
//    private var loadedDetailPostId: Int? = null
//
//    // -------------------------
//    // Public: create/edit/delete events
//    // -------------------------
//    private val _createResult = MutableSharedFlow<PostCreateResponse?>(extraBufferCapacity = 1)
//    val createResult: SharedFlow<PostCreateResponse?> = _createResult.asSharedFlow()
//
//    private val _editResult = MutableSharedFlow<PostEditResponse?>(extraBufferCapacity = 1)
//    val editResult: SharedFlow<PostEditResponse?> = _editResult.asSharedFlow()
//
//    sealed class DeleteEvent {
//        data class Success(val postId: Int) : DeleteEvent()
//        data class Fail(val message: String) : DeleteEvent()
//    }
//
//    private val _deleteEvent = MutableSharedFlow<DeleteEvent>(extraBufferCapacity = 1)
//    val deleteEvent: SharedFlow<DeleteEvent> = _deleteEvent.asSharedFlow()
//
//    // =========================================================
//    // List
//    // =========================================================
//    fun loadPosts(keyword: String?, filter: PostQueryFilter) {
//        viewModelScope.launch {
//            try {
//                val kw = keyword?.trim()!!
//                if (kw.isEmpty()) {
//                    _postList.value = emptyList()
//                    _message.tryEmit("검색어가 비어있습니다.")
//                    return@launch
//                }
//
//                val apiRes = repository.getPosts(keyword = kw, filter = filter)
//                val data = apiRes.data
//                if (data != null) {
//                    _postList.value = data!!
//                } else {
//                    _postList.value = emptyList()
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "게시글을 불러오지 못했습니다.")
//                }
//            } catch (e: Exception) {
//                _postList.value = emptyList()
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    fun loadPostsFilter(filter: PostQueryFilter) {
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.getPostsFilter(filter)
//                val data = apiRes.data
//                if (data != null) {
//                    _postList.value = data!!
//                } else {
//                    _postList.value = emptyList()
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "게시글을 불러오지 못했습니다.")
//                }
//            } catch (e: Exception) {
//                _postList.value = emptyList()
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Detail
//    // =========================================================
//    fun loadPostDetail(postId: Int, force: Boolean = false) {
//        if (!force && loadedDetailPostId == postId && _postDetail.value != null) return
//        loadedDetailPostId = postId
//
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.getPostDetail(postId)
//
//                if (apiRes.httpStatusCode in 200..299 && apiRes.data != null) {
//                    _postDetail.value = apiRes.data
//                } else {
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "게시글 상세 조회 실패")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "loadPostDetail 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Like (✅ 조회수 증가 버그 방지: 상세 재조회 금지)
//    // =========================================================
//    fun toggleLike(postId: Int) {
//        viewModelScope.launch {
//            val cur = _postDetail.value
//            if (cur == null || cur.postId != postId) return@launch
//
//            val willLike = !cur.isLiked
//
//            try {
//                val apiRes = if (willLike) repository.likePost(postId) else repository.unlikePost(postId)
//                val ok = apiRes.httpStatusCode in 200..299
//
//                if (!ok) {
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "좋아요 처리 실패")
//                    return@launch
//                }
//
//                // ✅ 서버 상세 재조회 안 함 (views 증가 방지)
//                val newCount = (cur.likeCount + if (willLike) 1 else -1).coerceAtLeast(0)
//                _postDetail.value = cur.copy(
//                    isLiked = willLike,
//                    likeCount = newCount
//                )
//            } catch (e: Exception) {
//                Log.e(TAG, "toggleLike 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Create
//    // =========================================================
//    fun createPost(photo: MultipartBody.Part, title: String, content: String, items: List<Int>) {
//        viewModelScope.launch {
//            try {
//                val res = repository.createPost(
//                    photo = photo,
//                    request = PostCreateRequest(title = title, content = content, items = items)
//                )
//
//                if (res.isSuccessful) {
//                    val body = res.body()
//                    _createResult.tryEmit(body?.data)
//                    _message.tryEmit(body?.responseMessage ?: body?.errorMessage ?: "등록 완료")
//                } else {
//                    val apiError = parseErrorBody<PostCreateResponse>(res)
//                    _message.tryEmit(apiError?.errorMessage ?: "등록 실패(code=${res.code()})")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "createPost 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Update
//    // =========================================================
//    fun editPost(postId: Int, photo: MultipartBody.Part?, title: String, content: String, items: List<Int>) {
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.editPost(
//                    postId = postId,
//                    photo = photo,
//                    request = PostEditRequest(title = title, content = content, items = items)
//                )
//
//                if (apiRes.data != null) _editResult.tryEmit(apiRes.data)
//                _message.tryEmit(apiRes.responseMessage ?: apiRes.errorMessage ?: "수정 완료")
//
//                // 수정 성공 시에만 상세 재조회 (이건 의도된 동작)
//                if (apiRes.httpStatusCode in 200..299) {
//                    loadPostDetail(postId, force = true)
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "editPost 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    // =========================================================
//    // Delete
//    // =========================================================
//    fun deletePost(postId: Int) {
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.deletePost(postId)
//                val ok = apiRes.httpStatusCode in 200..299
//
//                if (ok) {
//                    _deleteEvent.tryEmit(DeleteEvent.Success(postId))
//                    _message.tryEmit(apiRes.responseMessage ?: "삭제 완료")
//                } else {
//                    val msg = apiRes.errorMessage ?: apiRes.responseMessage ?: "삭제 실패"
//                    _deleteEvent.tryEmit(DeleteEvent.Fail(msg))
//                    _message.tryEmit(msg)
//                }
//            } catch (e: Exception) {
//                val msg = e.message ?: "네트워크 오류"
//                _deleteEvent.tryEmit(DeleteEvent.Fail(msg))
//                _message.tryEmit(msg)
//            }
//        }
//    }
//
//    // =========================================================
//    // (선택) 옷 저장/해제
//    // =========================================================
//    fun toggleClothesSave(postId: Int, clothesId: Int, willSave: Boolean) {
//        viewModelScope.launch {
//            try {
//                val res = if (willSave) {
//                    repository.postClothesRental(clothesId)
//                } else {
//                    repository.deleteClothesRental(clothesId)
//                }
//
//                if (res.isSuccessful) {
//                    val msg = res.body()?.responseMessage ?: if (willSave) "저장 완료" else "저장 해제 완료"
//                    _message.tryEmit(msg)
//
//                    // 옷 저장 상태는 상세 items에 영향 → 상세 재조회 (서버가 views+1 하는 구조면 이 부분도 백엔드가 분리돼야 완벽)
//                    loadPostDetail(postId, force = true)
//                } else {
//                    _message.tryEmit("요청 실패(code=${res.code()})")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "toggleClothesSave 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//            }
//        }
//    }
//
//    private inline fun <reified T> parseErrorBody(res: Response<ApiResponse<T>>): ApiResponse<T>? {
//        return try {
//            val json = res.errorBody()?.string() ?: return null
//            val type = object : TypeToken<ApiResponse<T>>() {}.type
//            Gson().fromJson<ApiResponse<T>>(json, type)
//        } catch (_: Exception) {
//            null
//        }
//    }
// }

@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.ssafy.closetory.homeActivity.post

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssafy.closetory.dto.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Response

private const val TAG = "PostViewModel_싸피"

class PostViewModel : ViewModel() {

    // -------------------------
    // Dependencies
    // -------------------------
    private val repository = PostRepository()

    // -------------------------
    // Public: message
    // -------------------------
    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val message: SharedFlow<String> = _message.asSharedFlow()

    // -------------------------
    // Public: list (LiveData)
    // -------------------------
    private val _postList = MutableLiveData<List<PostItemResponse>>(emptyList())
    val postList: LiveData<List<PostItemResponse>> = _postList

    // -------------------------
    // Public: detail (StateFlow)
    // -------------------------
    private val _postDetail = MutableStateFlow<PostDetailResponse?>(null)
    val postDetail: StateFlow<PostDetailResponse?> = _postDetail.asStateFlow()

    private var loadedDetailPostId: Int? = null

    // -------------------------
    // Public: comments (StateFlow) ✅ 추가
    // -------------------------
    private val _comments = MutableStateFlow<List<CommentDto>>(emptyList())
    val comments: StateFlow<List<CommentDto>> = _comments.asStateFlow()

    // -------------------------
    // Public: create/edit/delete events
    // -------------------------
    private val _createResult = MutableSharedFlow<PostCreateResponse?>(extraBufferCapacity = 1)
    val createResult: SharedFlow<PostCreateResponse?> = _createResult.asSharedFlow()

    private val _editResult = MutableSharedFlow<PostEditResponse?>(extraBufferCapacity = 1)
    val editResult: SharedFlow<PostEditResponse?> = _editResult.asSharedFlow()

    sealed class DeleteEvent {
        data class Success(val postId: Int) : DeleteEvent()
        data class Fail(val message: String) : DeleteEvent()
    }

    private val _deleteEvent = MutableSharedFlow<DeleteEvent>(extraBufferCapacity = 1)
    val deleteEvent: SharedFlow<DeleteEvent> = _deleteEvent.asSharedFlow()

    // =========================================================
    // List
    // =========================================================
    fun loadPosts(keyword: String?, filter: PostQueryFilter) {
        viewModelScope.launch {
            try {
                val kw = keyword?.trim()!!
                if (kw.isEmpty()) {
                    _postList.value = emptyList()
                    _message.tryEmit("검색어가 비어있습니다.")
                    return@launch
                }

                val apiRes = repository.getPosts(keyword = kw, filter = filter)
                val data = apiRes.data
                if (data != null) {
                    _postList.value = data!!
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

    fun loadPostsFilter(filter: PostQueryFilter) {
        viewModelScope.launch {
            try {
                val apiRes = repository.getPostsFilter(filter)
                val data = apiRes.data
                if (data != null) {
                    _postList.value = data!!
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
    // Like (✅ 조회수 증가 버그 방지: 상세 재조회 금지)
    // =========================================================
    fun toggleLike(postId: Int) {
        viewModelScope.launch {
            val cur = _postDetail.value
            if (cur == null || cur.postId != postId) return@launch

            val willLike = !cur.isLiked

            try {
                val apiRes = if (willLike) repository.likePost(postId) else repository.unlikePost(postId)
                val ok = apiRes.httpStatusCode in 200..299

                if (!ok) {
                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "좋아요 처리 실패")
                    return@launch
                }

                // ✅ 서버 상세 재조회 안 함 (views 증가 방지)
                val newCount = (cur.likeCount + if (willLike) 1 else -1).coerceAtLeast(0)
                _postDetail.value = cur.copy(
                    isLiked = willLike,
                    likeCount = newCount
                )
            } catch (e: Exception) {
                Log.e(TAG, "toggleLike 예외", e)
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

                // 수정 성공 시에만 상세 재조회 (이건 의도된 동작)
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
                    val msg = res.body()?.responseMessage ?: if (willSave) "저장 완료" else "저장 해제 완료"
                    _message.tryEmit(msg)

                    // 옷 저장 상태는 상세 items에 영향 → 상세 재조회 (서버가 views+1 하는 구조면 이 부분도 백엔드가 분리라야 완벽)
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
    // Comments (✅ 댓글 기능 추가)
    // =========================================================

    // 댓글 목록 조회
//    fun loadComments(postId: Int) {
//        viewModelScope.launch {
//            try {
//                val apiRes = repository.getComments(postId)
//
//                if (apiRes.httpStatusCode in 200..299 && apiRes.data != null) {
//                    _comments.value = apiRes.data // .comment 제거
//                    Log.d(TAG, "댓글 조회 성공: ${apiRes.data.size}개")
//                } else {
//                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "댓글을 불러올 수 없습니다.")
//                    _comments.value = emptyList()
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "loadComments 예외", e)
//                _message.tryEmit(e.message ?: "네트워크 오류")
//                _comments.value = emptyList()
//            }
//        }
//    }

    /**
     * 댓글 목록 조회
     */
    fun loadComments(postId: Int) {
        viewModelScope.launch {
            try {
                val apiRes = repository.getComments(postId)

                if (apiRes.httpStatusCode in 200..299 && apiRes.data != null) {
                    Log.d(TAG, "========================================")
                    Log.d(TAG, "📋 댓글 조회 성공: ${apiRes.data.size}개")

                    // ✅ 각 댓글의 isMine 값 확인
                    apiRes.data.forEachIndexed { index, comment ->
                        Log.d(
                            TAG,
                            "댓글[$index] - ID: ${comment.commentId}, 닉네임: ${comment.nickname}, isMine: ${comment.isMine}, 내용: ${comment.content}"
                        )
                    }
                    Log.d(TAG, "========================================")

                    _comments.value = apiRes.data
                } else {
                    _message.tryEmit(apiRes.errorMessage ?: apiRes.responseMessage ?: "댓글을 불러올 수 없습니다.")
                    _comments.value = emptyList()
                    Log.e(
                        TAG,
                        "댓글 조회 실패 - httpStatusCode: ${apiRes.httpStatusCode}, errorMessage: ${apiRes.errorMessage}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadComments 예외", e)
                _message.tryEmit(e.message ?: "네트워크 오류")
                _comments.value = emptyList()
            }
        }
    }

    /**
     * 댓글 등록
     */
    fun createComment(postId: Int, content: String) {
        if (content.isBlank()) {
            viewModelScope.launch {
                _message.tryEmit("댓글 내용을 입력하세요.")
            }
            return
        }

        viewModelScope.launch {
            try {
                val res = repository.createComment(postId, content)

                if (res.isSuccessful) {
                    val msg = res.body()?.responseMessage ?: "댓글이 등록되었습니다."
                    _message.tryEmit(msg)

                    // 댓글 등록 후 목록 다시 조회
                    loadComments(postId)
                } else {
                    val errBody = runCatching { res.errorBody()?.string() }.getOrNull()
                    Log.e(TAG, "댓글 등록 실패: code=${res.code()}, errorBody=$errBody")

                    val errorMsg = when (res.code()) {
                        400 -> "댓글 내용은 비워둘 수 없습니다."
                        404 -> "존재하지 않는 게시글입니다."
                        else -> "댓글 등록에 실패했습니다."
                    }
                    _message.tryEmit(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "createComment 예외", e)
                _message.tryEmit(e.message ?: "네트워크 오류")
            }
        }
    }

    /**
     * 댓글 수정
     */
    fun updateComment(postId: Int, commentId: Int, content: String) {
        if (content.isBlank()) {
            viewModelScope.launch {
                _message.tryEmit("댓글 내용을 입력하세요.")
            }
            return
        }

        viewModelScope.launch {
            try {
                val res = repository.updateComment(postId, commentId, content)

                if (res.isSuccessful) {
                    val msg = res.body()?.responseMessage ?: "댓글이 수정되었습니다."
                    _message.tryEmit(msg)

                    // 댓글 수정 후 목록 다시 조회
                    loadComments(postId)
                } else {
                    val errBody = runCatching { res.errorBody()?.string() }.getOrNull()
                    Log.e(TAG, "댓글 수정 실패: code=${res.code()}, errorBody=$errBody")

                    val errorMsg = when (res.code()) {
                        400 -> "댓글 내용은 비워둘 수 없습니다."
                        401 -> "인증에 실패했습니다."
                        403 -> "댓글 수정 권한이 없습니다."
                        404 -> "댓글을 찾을 수 없습니다."
                        else -> "댓글 수정에 실패했습니다."
                    }
                    _message.tryEmit(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateComment 예외", e)
                _message.tryEmit(e.message ?: "네트워크 오류")
            }
        }
    }

    /**
     * 댓글 삭제
     */
    fun deleteComment(postId: Int, commentId: Int) {
        viewModelScope.launch {
            try {
                val res = repository.deleteComment(postId, commentId)

                if (res.isSuccessful) {
                    val msg = res.body()?.responseMessage ?: "댓글이 삭제되었습니다."
                    _message.tryEmit(msg)

                    // 댓글 삭제 후 목록 다시 조회
                    loadComments(postId)
                } else {
                    val errBody = runCatching { res.errorBody()?.string() }.getOrNull()
                    Log.e(TAG, "댓글 삭제 실패: code=${res.code()}, errorBody=$errBody")

                    val errorMsg = when (res.code()) {
                        401 -> "인증에 실패했습니다."
                        403 -> "댓글 삭제 권한이 없습니다."
                        404 -> "댓글을 찾을 수 없습니다."
                        else -> "댓글 삭제에 실패했습니다."
                    }
                    _message.tryEmit(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteComment 예외", e)
                _message.tryEmit(e.message ?: "네트워크 오류")
            }
        }
    }

    // =========================================================
    // Helper
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
