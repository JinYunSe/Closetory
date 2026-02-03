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
    suspend fun getPosts(keyword: String, filter: PostQueryFilter): ApiResponse<List<PostItemResponse>> = safeCall {
        service.getPosts(keyword = keyword, searchFilter = filter.name)
    }

    suspend fun getPostsFilter(filter: PostQueryFilter): ApiResponse<List<PostItemResponse>> = safeCall {
        service.getPostsFilter(searchFilter = filter.name)
    }

    // -------------------------
    // Detail
    // -------------------------
    suspend fun getPostDetail(postId: Int): ApiResponse<PostDetailResponse> = safeCall { service.getPostDetail(postId) }

    // -------------------------
    // Like
    // -------------------------
    suspend fun likePost(postId: Int): ApiResponse<Unit> = safeCall { service.likePost(postId) }

    suspend fun unlikePost(postId: Int): ApiResponse<Unit> = safeCall { service.unlikePost(postId) }

    // -------------------------
    // Create
    // -------------------------
    suspend fun createPost(
        photo: MultipartBody.Part,
        request: PostCreateRequest
    ): Response<ApiResponse<PostCreateResponse>> {
        val json = Gson().toJson(request)
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return service.createPost(photo = photo, request = body)
    }

    // -------------------------
    // Update
    // -------------------------
    suspend fun editPost(
        postId: Int,
        photo: MultipartBody.Part?,
        request: PostEditRequest
    ): ApiResponse<PostEditResponse> = safeCall {
        val json = Gson().toJson(request)
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        service.editPost(postId = postId, photo = photo, request = body)
    }

    // -------------------------
    // Delete
    // -------------------------
    suspend fun deletePost(postId: Int): ApiResponse<Unit> = safeCall { service.deletePost(postId) }

    // -------------------------
    // Clothes save/unsave
    // -------------------------
    suspend fun postClothesRental(clothesId: Int): Response<ApiResponse<Unit>> = service.postClothesRental(clothesId)

    suspend fun deleteClothesRental(clothesId: Int): Response<ApiResponse<Unit>> =
        service.deleteClothesRental(clothesId)

    private inline fun <T> safeCall(block: () -> ApiResponse<T>): ApiResponse<T> = try {
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
}
