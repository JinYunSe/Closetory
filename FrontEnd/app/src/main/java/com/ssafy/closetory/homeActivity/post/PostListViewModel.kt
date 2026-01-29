package com.ssafy.closetory.homeActivity.post
//
import androidx.lifecycle.ViewModel
// import androidx.lifecycle.viewModelScope
// import com.ssafy.closetory.dto.PostItemResponse
// import com.ssafy.closetory.dto.PostQueryFilter
// import kotlinx.coroutines.flow.MutableSharedFlow
// import kotlinx.coroutines.flow.MutableStateFlow
// import kotlinx.coroutines.flow.SharedFlow
// import kotlinx.coroutines.flow.StateFlow
// import kotlinx.coroutines.launch
//
class PostListViewModel : ViewModel() {
//
//    private val repository = PostRepository()
//
//    // 게시글 목록 상태
//    private val _postList = MutableStateFlow<List<PostItemResponse>>(emptyList())
//    val postList: StateFlow<List<PostItemResponse>> = _postList
//
//    // 로딩 상태
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading
//
//    // 단발성 메시지 (토스트/스낵바 등)
//    private val _message = MutableSharedFlow<String>()
//    val message: SharedFlow<String> = _message
//
//    // 게시글 목록/검색 요청
//    fun loadPosts(keyword: String?, filter: PostQueryFilter) {
//        viewModelScope.launch {
//            _isLoading.value = true
//
//            val apiRes = repository.getPosts(
//                keyword = keyword,
//                filter = filter
//            )
//
//            val data = apiRes.data
//            if (data != null) {
//                // 성공: 리스트 갱신
//                _postList.value = data
//            } else {
//                // 실패: 단발성 메시지 emit
//                val msg = apiRes.errorMessage
//                    ?: apiRes.responseMessage
//                    ?: "게시글을 불러오지 못했습니다."
//                _message.emit(msg)
//            }
//
//            _isLoading.value = false
//        }
//    }
}
