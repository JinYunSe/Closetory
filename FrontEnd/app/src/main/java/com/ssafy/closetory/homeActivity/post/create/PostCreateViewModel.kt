package com.ssafy.closetory.homeActivity.post.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.PostCreateRequest
import com.ssafy.closetory.dto.PostCreateResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Response

private const val TAG = "PostCreateViewModel_싸피"

class PostCreateViewModel : ViewModel() {

    private val repository = PostCreateRepository() // 네트워크 호출 담당 Repository

    private val _createResult = MutableSharedFlow<PostCreateResponse?>(extraBufferCapacity = 1) // 등록 성공 결과 1회 전달
    val createResult = _createResult.asSharedFlow()

    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1) // 토스트/스낵바 메시지 1회 전달
    val message = _message.asSharedFlow()

    private val _isLoading = MutableSharedFlow<Boolean>(extraBufferCapacity = 1) // 로딩 상태 전달(선택)
    val isLoading = _isLoading.asSharedFlow()

    // 게시글 등록(멀티파트 전송) 요청
    fun createPost(photo: MultipartBody.Part, title: String, content: String, items: List<Int>) {
        viewModelScope.launch {
            _isLoading.tryEmit(true)

            try {
                Log.d(
                    TAG,
                    "게시글 등록 Request 전송 확인  : photo : $photo, title : $title, content : $content, items : $items"
                )

                val res = repository.createPost(
                    photo = photo,
                    request = PostCreateRequest(
                        title = title,
                        content = content,
                        items = items
                    )
                )

                if (res.isSuccessful) {
                    val body = res.body()
                    _createResult.tryEmit(body?.data)

                    // 성공 시 하드코딩 없이 서버 responseMessage 그대로 출력
                    val msg = body?.responseMessage
                        ?: body?.errorMessage
                        ?: "응답 메시지가 없습니다."
                    _message.tryEmit(msg)
                } else {
                    val apiError = parseErrorBody(res)
                    // 실패 시 errorMessage 그대로 출력
                    val msg =
                        apiError?.errorMessage
                            ?: "요청 실패 (code=${res.code()})"
                    _message.tryEmit(msg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "createPost exception", e)
                _message.tryEmit(e.message ?: "네트워크 오류")
            } finally {
                _isLoading.tryEmit(false)
            }
        }
    }

    // errorBody()를 ApiResponse 형태로 파싱
    private fun parseErrorBody(res: Response<ApiResponse<PostCreateResponse>>): ApiResponse<PostCreateResponse>? {
        return try {
            val json = res.errorBody()?.string() ?: return null
            val type = object : TypeToken<ApiResponse<PostCreateResponse>>() {}.type
            Gson().fromJson<ApiResponse<PostCreateResponse>>(json, type)
        } catch (_: Exception) {
            null
        }
    }
}
