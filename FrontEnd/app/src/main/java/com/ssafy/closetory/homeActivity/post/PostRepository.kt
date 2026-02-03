package com.ssafy.closetory.homeActivity.post

import android.util.Log
import com.google.gson.Gson
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.PostCreateRequest
import com.ssafy.closetory.dto.PostCreateResponse
import com.ssafy.closetory.dto.PostDetailResponse
import com.ssafy.closetory.dto.PostEditRequest
import com.ssafy.closetory.dto.PostEditResponse
import com.ssafy.closetory.dto.PostItemResponse
import com.ssafy.closetory.dto.PostQueryFilter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

private const val TAG = "PostRepository_싸피"

class PostRepository {

    private val service: PostService by lazy {
        // 너희 프로젝트에서 retrofit 인스턴스 만드는 방식에 맞춰 수정 가능
        ApplicationClass.retrofit.create(PostService::class.java)
    }

    // -------------------------
    // Read - List
    // -------------------------
    suspend fun getPosts(keyword: String?, filter: PostQueryFilter): ApiResponse<List<PostItemResponse>> = safeCall {
        service.getPosts(
            keyword = keyword,
            filter = filter.name
        )
    }

    // -------------------------
    // Read - List (필터만)
    // -------------------------
    suspend fun getPostsFilter(filter: PostQueryFilter): ApiResponse<List<PostItemResponse>> = safeCall {
        service.getPostsFilter(
            searchFilter = filter.name
        )
    }

    // -------------------------
    // Read - Detail
    // -------------------------
    suspend fun getPostDetail(postId: Int): ApiResponse<PostDetailResponse> = safeCall {
        service.getPostDetail(postId)
    }

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
    suspend fun deletePost(postId: Int): ApiResponse<Unit> = safeCall {
        service.deletePost(postId)
    }

    // -------------------------
    // 옷 저장/해제 (Response 그대로 반환)
    // -------------------------
    suspend fun postClothesRental(clothesId: Int): Response<ApiResponse<Unit>> = service.postClothesRental(clothesId)

    suspend fun deleteClothesRental(clothesId: Int): Response<ApiResponse<Unit>> =
        service.deleteClothesRental(clothesId)

    // -------------------------
    // 공용 safe wrapper (예외 -> ApiResponse 형태로 통일)
    // -------------------------
    private inline fun <T> safeCall(block: () -> ApiResponse<T>): ApiResponse<T> = try {
        block()
    } catch (e: Exception) {
        Log.e(TAG, "API call error", e)
        ApiResponse(
            httpStatusCode = 500,
            responseMessage = null,
            errorMessage = e.message ?: "네트워크 오류",
            data = null
        )
    }
}
