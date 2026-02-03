// package com.ssafy.closetory.homeActivity.post
//
// import android.util.Log
// import com.google.gson.Gson
// import com.ssafy.closetory.ApplicationClass
// import com.ssafy.closetory.dto.*
// import okhttp3.MediaType.Companion.toMediaType
// import okhttp3.MultipartBody
// import okhttp3.RequestBody.Companion.toRequestBody
// import retrofit2.Response
//
// private const val TAG = "PostRepository_싸피"
//
// class PostRepository {
//
//    private val service: PostService by lazy {
//        ApplicationClass.retrofit.create(PostService::class.java)
//    }
//
//    // -------------------------
//    // List
//    // -------------------------
//    suspend fun getPosts(keyword: String, filter: PostQueryFilter): ApiResponse<List<PostItemResponse>> = safeCall {
//        service.getPosts(keyword = keyword, searchFilter = filter.name)
//    }
//
//    suspend fun getPostsFilter(filter: PostQueryFilter): ApiResponse<List<PostItemResponse>> = safeCall {
//        service.getPostsFilter(searchFilter = filter.name)
//    }
//
//    // -------------------------
//    // Detail
//    // -------------------------
//    suspend fun getPostDetail(postId: Int): ApiResponse<PostDetailResponse> = safeCall { service.getPostDetail(postId) }
//
//    // -------------------------
//    // Like
//    // -------------------------
//    suspend fun likePost(postId: Int): ApiResponse<Unit> = safeCall { service.likePost(postId) }
//
//    suspend fun unlikePost(postId: Int): ApiResponse<Unit> = safeCall { service.unlikePost(postId) }
//
//    // -------------------------
//    // Create
//    // -------------------------
//    suspend fun createPost(
//        photo: MultipartBody.Part,
//        request: PostCreateRequest
//    ): Response<ApiResponse<PostCreateResponse>> {
//        val json = Gson().toJson(request)
//        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
//        return service.createPost(photo = photo, request = body)
//    }
//
//    // -------------------------
//    // Update
//    // -------------------------
//    suspend fun editPost(
//        postId: Int,
//        photo: MultipartBody.Part?,
//        request: PostEditRequest
//    ): ApiResponse<PostEditResponse> = safeCall {
//        val json = Gson().toJson(request)
//        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
//        service.editPost(postId = postId, photo = photo, request = body)
//    }
//
//    // -------------------------
//    // Delete
//    // -------------------------
//    suspend fun deletePost(postId: Int): ApiResponse<Unit> = safeCall { service.deletePost(postId) }
//
//    // -------------------------
//    // Clothes save/unsave
//    // -------------------------
//    suspend fun postClothesRental(clothesId: Int): Response<ApiResponse<Unit>> = service.postClothesRental(clothesId)
//
//    suspend fun deleteClothesRental(clothesId: Int): Response<ApiResponse<Unit>> =
//        service.deleteClothesRental(clothesId)
//
//    private inline fun <T> safeCall(block: () -> ApiResponse<T>): ApiResponse<T> = try {
//        block()
//    } catch (e: Exception) {
//        Log.e(TAG, "API error", e)
//        ApiResponse(
//            httpStatusCode = 500,
//            responseMessage = null,
//            errorMessage = e.message ?: "네트워크 오류",
//            data = null
//        )
//    }
// }

// package com.ssafy.closetory.homeActivity.post
//
// import android.util.Log
// import com.google.gson.Gson
// import com.ssafy.closetory.ApplicationClass
// import com.ssafy.closetory.dto.*
// import okhttp3.MediaType.Companion.toMediaType
// import okhttp3.MultipartBody
// import okhttp3.RequestBody.Companion.toRequestBody
// import retrofit2.Response
//
// private const val TAG = "PostRepository_싸피"
//
// class PostRepository {
//
//    private val service: PostService by lazy {
//        ApplicationClass.retrofit.create(PostService::class.java)
//    }
//
//    // -------------------------
//    // List
//    // -------------------------
//    suspend fun getPosts(keyword: String, filter: PostQueryFilter): ApiResponse<List<PostItemResponse>> = safeCall {
//        service.getPosts(keyword = keyword, searchFilter = filter.name)
//    }
//
//    suspend fun getPostsFilter(filter: PostQueryFilter): ApiResponse<List<PostItemResponse>> = safeCall {
//        service.getPostsFilter(searchFilter = filter.name)
//    }
//
//    // -------------------------
//    // Detail
//    // -------------------------
//    suspend fun getPostDetail(postId: Int): ApiResponse<PostDetailResponse> = safeCall { service.getPostDetail(postId) }
//
//    // -------------------------
//    // Like
//    // -------------------------
//    suspend fun likePost(postId: Int): ApiResponse<Unit> = safeCall { service.likePost(postId) }
//
//    suspend fun unlikePost(postId: Int): ApiResponse<Unit> = safeCall { service.unlikePost(postId) }
//
//    // -------------------------
//    // Create
//    // -------------------------
//    suspend fun createPost(
//        photo: MultipartBody.Part,
//        request: PostCreateRequest
//    ): Response<ApiResponse<PostCreateResponse>> {
//        val json = Gson().toJson(request)
//        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
//        return service.createPost(photo = photo, request = body)
//    }
//
//    // -------------------------
//    // Update
//    // -------------------------
//    suspend fun editPost(
//        postId: Int,
//        photo: MultipartBody.Part?,
//        request: PostEditRequest
//    ): ApiResponse<PostEditResponse> = safeCall {
//        val json = Gson().toJson(request)
//        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
//        service.editPost(postId = postId, photo = photo, request = body)
//    }
//
//    // -------------------------
//    // Delete
//    // -------------------------
//    suspend fun deletePost(postId: Int): ApiResponse<Unit> = safeCall { service.deletePost(postId) }
//
//    // -------------------------
//    // Clothes save/unsave
//    // -------------------------
//    suspend fun postClothesRental(clothesId: Int): Response<ApiResponse<Unit>> = service.postClothesRental(clothesId)
//
//    suspend fun deleteClothesRental(clothesId: Int): Response<ApiResponse<Unit>> =
//        service.deleteClothesRental(clothesId)
//
//    private inline fun <T> safeCall(block: () -> ApiResponse<T>): ApiResponse<T> = try {
//        block()
//    } catch (e: Exception) {
//        Log.e(TAG, "API error", e)
//        ApiResponse(
//            httpStatusCode = 500,
//            responseMessage = null,
//            errorMessage = e.message ?: "네트워크 오류",
//            data = null
//        )
//    }
// }

// package com.ssafy.closetory.homeActivity.post
//
// import android.util.Log
// import com.google.gson.Gson
// import com.ssafy.closetory.ApplicationClass
// import com.ssafy.closetory.dto.*
// import okhttp3.MediaType.Companion.toMediaType
// import okhttp3.MultipartBody
// import okhttp3.RequestBody.Companion.toRequestBody
// import retrofit2.Response
//
// private const val TAG = "PostRepository_싸피"
//
// class PostRepository {
//
//    private val service: PostService by lazy {
//        ApplicationClass.retrofit.create(PostService::class.java)
//    }
//
//    // -------------------------
//    // List
//    // -------------------------
//    suspend fun getPosts(keyword: String, filter: PostQueryFilter): ApiResponse<List<PostItemResponse>> = safeCall {
//        service.getPosts(keyword = keyword, searchFilter = filter.name)
//    }
//
//    suspend fun getPostsFilter(filter: PostQueryFilter): ApiResponse<List<PostItemResponse>> = safeCall {
//        service.getPostsFilter(searchFilter = filter.name)
//    }
//
//    // -------------------------
//    // Detail
//    // -------------------------
//    suspend fun getPostDetail(postId: Int): ApiResponse<PostDetailResponse> = safeCall { service.getPostDetail(postId) }
//
//    // -------------------------
//    // Like
//    // -------------------------
//    suspend fun likePost(postId: Int): ApiResponse<Unit> = safeCall { service.likePost(postId) }
//
//    suspend fun unlikePost(postId: Int): ApiResponse<Unit> = safeCall { service.unlikePost(postId) }
//
//    // -------------------------
//    // Create
//    // -------------------------
//    suspend fun createPost(
//        photo: MultipartBody.Part,
//        request: PostCreateRequest
//    ): Response<ApiResponse<PostCreateResponse>> {
//        val json = Gson().toJson(request)
//        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
//        return service.createPost(photo = photo, request = body)
//    }
//
//    // -------------------------
//    // Update
//    // -------------------------
//    suspend fun editPost(
//        postId: Int,
//        photo: MultipartBody.Part?,
//        request: PostEditRequest
//    ): ApiResponse<PostEditResponse> = safeCall {
//        val json = Gson().toJson(request)
//        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
//        service.editPost(postId = postId, photo = photo, request = body)
//    }
//
//    // -------------------------
//    // Delete
//    // -------------------------
//    suspend fun deletePost(postId: Int): ApiResponse<Unit> = safeCall { service.deletePost(postId) }
//
//    // -------------------------
//    // Clothes save/unsave
//    // -------------------------
//    suspend fun postClothesRental(clothesId: Int): Response<ApiResponse<Unit>> = service.postClothesRental(clothesId)
//
//    suspend fun deleteClothesRental(clothesId: Int): Response<ApiResponse<Unit>> =
//        service.deleteClothesRental(clothesId)
//
//    private inline fun <T> safeCall(block: () -> ApiResponse<T>): ApiResponse<T> = try {
//        block()
//    } catch (e: Exception) {
//        Log.e(TAG, "API error", e)
//        ApiResponse(
//            httpStatusCode = 500,
//            responseMessage = null,
//            errorMessage = e.message ?: "네트워크 오류",
//            data = null
//        )
//    }
// }

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

    // -------------------------
    // Comments (✅ 댓글 API 추가)
    // -------------------------

    /**
     * 댓글 목록 조회
     * ✅ 수정: 서버가 CommentListResponse 반환 → comment 필드 추출
     */
    suspend fun getComments(postId: Int): ApiResponse<List<CommentDto>> = safeCall {
        service.getComments(postId)
    }

    /**
     * 댓글 등록
     */
    suspend fun createComment(postId: Int, content: String): Response<ApiResponse<CommentCreateResponse>> {
        val request = CommentCreateRequest(content)
        return service.createComment(postId, request)
    }

    /**
     * 댓글 수정
     */
    suspend fun updateComment(
        postId: Int,
        commentId: Int,
        content: String
    ): Response<ApiResponse<CommentUpdateResponse>> {
        val request = CommentUpdateRequest(content)
        return service.updateComment(postId, commentId, request)
    }

    /**
     * 댓글 삭제
     */
    suspend fun deleteComment(postId: Int, commentId: Int): Response<ApiResponse<Unit>> =
        service.deleteComment(postId, commentId)

    // -------------------------
    // Helper
    // -------------------------
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
