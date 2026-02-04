@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.ssafy.closetory.homeActivity.post

import android.util.Log
import com.google.gson.Gson
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

private const val TAG = "PostRepository_싸피"

class PostRepository {

    private val service: PostService by lazy {
        ApplicationClass.retrofit.create(PostService::class.java)
    }

    // -------------------------
    // List
    // -------------------------
    suspend fun getPosts(keyword: String, filter: PostQueryFilter): ApiResponse<List<PostItemResponse>> =
        safeApiCall { service.getPosts(keyword = keyword, searchFilter = filter.name) }

    suspend fun getPostsFilter(filter: PostQueryFilter): ApiResponse<List<PostItemResponse>> =
        safeApiCall { service.getPostsFilter(searchFilter = filter.name) }

    // -------------------------
    // Detail
    // -------------------------
    suspend fun getPostDetail(postId: Int): ApiResponse<PostDetailResponse> =
        safeApiCall { service.getPostDetail(postId) }

    // -------------------------
    // Like
    // -------------------------
    suspend fun likePost(postId: Int): ApiResponse<Unit> = safeApiCall { service.likePost(postId) }

    suspend fun unlikePost(postId: Int): ApiResponse<Unit> = safeApiCall { service.unlikePost(postId) }

    // -------------------------
    // Create
    // -------------------------
    suspend fun createPost(photo: MultipartBody.Part, request: PostCreateRequest): ApiResponse<PostCreateResponse> =
        safeResponseCall {
            val json = Gson().toJson(request)
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            service.createPost(photo = photo, request = body)
        }

    // -------------------------
    // Update
    // -------------------------
    suspend fun editPost(
        postId: Int,
        photo: MultipartBody.Part?,
        request: PostEditRequest
    ): ApiResponse<PostEditResponse> = safeApiCall {
        val json = Gson().toJson(request)
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        service.editPost(postId = postId, photo = photo, request = body)
    }

    // -------------------------
    // Delete
    // -------------------------
    suspend fun deletePost(postId: Int): ApiResponse<Unit> = safeApiCall { service.deletePost(postId) }

    // -------------------------
    // Clothes save/unsave
    // -------------------------
    suspend fun postClothesRental(clothesId: Int): ApiResponse<Unit> =
        safeResponseCall { service.postClothesRental(clothesId) }

    suspend fun deleteClothesRental(clothesId: Int): ApiResponse<Unit> =
        safeResponseCall { service.deleteClothesRental(clothesId) }

    // -------------------------
    // Comments (✅ FIX: 서버가 List<CommentDto>를 직접 반환)
    // -------------------------
    suspend fun getComments(postId: Int): ApiResponse<List<CommentDto>> = safeApiCall { service.getComments(postId) }

    suspend fun createComment(postId: Int, content: String): ApiResponse<CommentCreateResponse> = safeResponseCall {
        val request = CommentCreateRequest(content)
        service.createComment(postId, request)
    }

    suspend fun updateComment(postId: Int, commentId: Int, content: String): ApiResponse<CommentUpdateResponse> =
        safeResponseCall {
            val request = CommentUpdateRequest(content)
            service.updateComment(postId, commentId, request)
        }

    suspend fun deleteComment(postId: Int, commentId: Int): ApiResponse<Unit> =
        safeResponseCall { service.deleteComment(postId, commentId) }

    // -------------------------
    // Helper (✅ 2종류로 분리: ApiResponse 직접 / Response<ApiResponse> 래핑)
    // -------------------------
    private suspend inline fun <T> safeApiCall(crossinline block: suspend () -> ApiResponse<T>): ApiResponse<T> = try {
        block()
    } catch (e: Exception) {
        Log.e(TAG, "API error", e)
        ApiResponse(
            httpStatusCode = 500,
            responseMessage = null,
            errorMessage = e.message ?: "네트워크 오류",
            data = null
        )
    }

    private suspend inline fun <T> safeResponseCall(
        crossinline block: suspend () -> Response<ApiResponse<T>>
    ): ApiResponse<T> = try {
        val res = block()

        if (res.isSuccessful) {
            res.body() ?: ApiResponse(
                httpStatusCode = 500,
                responseMessage = null,
                errorMessage = "빈 응답(body=null)",
                data = null
            )
        } else {
            // ✅ 서버가 4xx/5xx 던질 때도 ApiResponse 형태로 통일해 UI가 한 방식으로 처리 가능
            ApiResponse(
                httpStatusCode = res.code(),
                responseMessage = null,
                errorMessage = "서버 오류: ${res.code()}",
                data = null
            )
        }
    } catch (e: Exception) {
        Log.e(TAG, "API error", e)
        ApiResponse(
            httpStatusCode = 500,
            responseMessage = null,
            errorMessage = e.message ?: "네트워크 오류",
            data = null
        )
    }
}
