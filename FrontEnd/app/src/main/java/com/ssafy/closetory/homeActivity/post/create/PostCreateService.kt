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

    // @Body JSON 방식 제거 → @Multipart로 파일 + 텍스트 전송
    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Part photoUrl: MultipartBody.Part, // 파일
        @Part("title") title: RequestBody, // 텍스트
        @Part("content") content: RequestBody, // 텍스트
        @Part("items") items: RequestBody // JSON 배열 문자열: "[1,2,3]"
    ): Response<ApiResponse<PostCreateResponse>>
}
