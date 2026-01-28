package com.ssafy.closetory.homeActivity.post.create

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.PostCreateResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PostCreateService {

    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Part image: MultipartBody.Part, // 파일
        @Part("title") title: RequestBody, // 문자열 파트
        @Part("content") content: RequestBody, // 문자열 파트
        @Part("items") items: RequestBody? // JSON 문자열 파트 (nullable)
    ): Response<ApiResponse<PostCreateResponse>>
}
