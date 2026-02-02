package com.ssafy.closetory.homeActivity.post

import android.util.Log
import com.google.gson.Gson
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.PostDetailResponse
import com.ssafy.closetory.dto.PostEditRequest
import com.ssafy.closetory.dto.PostEditResponse
import com.ssafy.closetory.dto.PostItemResponse
import com.ssafy.closetory.dto.PostQueryFilter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

private const val TAG = "PostRepository_싸피"

// 게시글 관련 Repository (목록/상세/작성 등에서 재사용 가능한 형태)
class PostRepository {

    // Retrofit Service 생성
    private val postService: PostService =
        ApplicationClass.retrofit.create(PostService::class.java)

    // 게시글 목록/검색 조회
    suspend fun getPosts(keyword: String?, searchfilter: PostQueryFilter): ApiResponse<List<PostItemResponse>> = try {
        val res = postService.getPosts(
            keyword = keyword,
            searchfilter = searchfilter
        )

        Log.d(TAG, "Posts 요청 : $res ")
        if (res.isSuccessful) {
            // 성공 응답 (ApiResponse<List<PostItemResponse>>)
            res.body() ?: ApiResponse(
                httpStatusCode = res.code(),
                responseMessage = null,
                errorMessage = "응답 바디가 비어있습니다.",
                data = null
            )
        } else {
            // 실패 응답 (서버가 errorBody를 ApiResponse 형태로 안 줄 수도 있어서 raw로 저장)
            val rawError = res.errorBody()?.string()
            ApiResponse(
                httpStatusCode = res.code(),
                responseMessage = null,
                errorMessage = rawError ?: "서버 오류가 발생했습니다.",
                data = null
            )
        }
    } catch (e: Exception) {
        // 네트워크 예외 등
        ApiResponse(
            httpStatusCode = -1,
            responseMessage = null,
            errorMessage = e.message ?: "네트워크 오류가 발생했습니다.",
            data = null
        )
    }

    // 게시글 상세 조회
    suspend fun getPostDetail(postId: Int): ApiResponse<PostDetailResponse> = try {
        val res = postService.getPostDetail(postId)
        if (res.isSuccessful) {
            res.body() ?: ApiResponse(res.code(), null, "응답 바디가 비어있습니다.", null)
        } else {
            val rawError = res.errorBody()?.string()
            ApiResponse(res.code(), null, rawError ?: "서버 오류가 발생했습니다.", null)
        }
    } catch (e: Exception) {
        ApiResponse(-1, null, e.message ?: "네트워크 오류가 발생했습니다.", null)
    }

    // 게시글 수정
    suspend fun editPost(
        postId: Int,
        photo: MultipartBody.Part?,
        request: PostEditRequest
    ): ApiResponse<PostEditResponse> = try {
        // request DTO를 JSON으로 변환해서 RequestBody로 만든다
        val json = Gson().toJson(request)
        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        Log.d(TAG, "editPost() request=$json")

        val res = postService.editPost(
            postId = postId,
            photo = photo,
            request = requestBody
        )

        if (res.isSuccessful) {
            res.body() ?: ApiResponse(
                httpStatusCode = res.code(),
                responseMessage = null,
                errorMessage = "응답 바디가 비어있습니다.",
                data = null
            )
        } else {
            val rawError = res.errorBody()?.string()
            Log.d(TAG, "editPost fail code=${res.code()} errorBody=$rawError")
            ApiResponse(
                httpStatusCode = res.code(),
                responseMessage = null,
                errorMessage = rawError ?: "서버 오류가 발생했습니다.",
                data = null
            )
        }
    } catch (e: Exception) {
        ApiResponse(
            httpStatusCode = -1,
            responseMessage = null,
            errorMessage = e.message ?: "네트워크 오류가 발생했습니다.",
            data = null
        )
    }

    // 게시글 삭제
    suspend fun deletePost(postId: Int): ApiResponse<Unit> = try {
        val res = postService.deletePost(postId)

        if (res.isSuccessful) {
            res.body() ?: ApiResponse(
                httpStatusCode = res.code(),
                responseMessage = null,
                errorMessage = "응답 바디가 비어있습니다.",
                data = null
            )
        } else {
            val rawError = res.errorBody()?.string()
            ApiResponse(
                httpStatusCode = res.code(),
                responseMessage = null,
                errorMessage = rawError ?: "서버 오류가 발생했습니다.",
                data = null
            )
        }
    } catch (e: Exception) {
        ApiResponse(
            httpStatusCode = -1,
            responseMessage = null,
            errorMessage = e.message ?: "네트워크 오류가 발생했습니다.",
            data = null
        )
    }
}
