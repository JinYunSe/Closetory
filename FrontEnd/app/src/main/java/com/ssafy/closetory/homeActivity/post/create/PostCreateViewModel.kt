package com.ssafy.closetory.homeActivity.post.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.PostCreateResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

private const val TAG = "PostCreateViewModel_싸피"

class PostCreateViewModel : ViewModel() {

    private val repository = PostCreateRepository()

    // 게시글 등록 결과(1회성)
    private val _createResult = MutableSharedFlow<PostCreateResponse?>(extraBufferCapacity = 1)
    val createResult = _createResult.asSharedFlow()

    // 토스트/스낵바용 메시지(1회성)
    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val message = _message.asSharedFlow()

    // 로딩이 필요하면 사용(선택)
    private val _isLoading = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val isLoading = _isLoading.asSharedFlow()

    // 입력 검증은 Fragment에서 끝내고, ViewModel은 "통신"만 담당
    // photoUrl(String) 제거 → imagePart + RequestBody 받기
    fun createPost(imagePart: MultipartBody.Part, title: RequestBody, content: RequestBody, items: RequestBody) {
        viewModelScope.launch {
            _isLoading.tryEmit(true)

            try {
                val res = repository.createPost(
                    imagePart = imagePart,
                    title = title,
                    content = content,
                    items = items
                )

                if (res.isSuccessful) {
                    val body = res.body()
                    val data = body?.data
                    Log.d(TAG, "createPost success data=$data")

                    _createResult.tryEmit(data)
                    _message.tryEmit(body?.responseMessage ?: "게시글 등록 성공")
                } else {
                    val apiError = parseErrorBody(res)
                    Log.d(TAG, "createPost fail code=${res.code()} error=$apiError")

                    _message.tryEmit(
                        apiError?.errorMessage
                            ?: apiError?.responseMessage
                            ?: "게시글 등록 실패 (code=${res.code()})"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "createPost exception", e)
                _message.tryEmit(e.message ?: "네트워크 오류")
            } finally {
                _isLoading.tryEmit(false)
            }
        }
    }

    // errorBody()를 ApiResponse 형태로 파싱 (서버가 공통 ApiResponse를 내려준다는 가정)
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
