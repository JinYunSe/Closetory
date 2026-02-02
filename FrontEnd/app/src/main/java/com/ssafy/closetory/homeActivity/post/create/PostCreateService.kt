package com.ssafy.closetory.homeActivity.post.create

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.PostCreateRequest
import com.ssafy.closetory.dto.PostCreateResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part

interface PostCreateService {

    @Multipart
    @PATCH("posts")
    suspend fun createPost(
        @Part photo: MultipartBody.Part, // 멀티파트 형식
        @Part("request") request: PostCreateRequest // JSON 형식 (title, content, items)
    ): Response<ApiResponse<PostCreateResponse>>
}
