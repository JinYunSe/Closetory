package com.ssafy.closetory.homeActivity.post.create

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.PostCreateResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class PostCreateRepository {

    private val service: PostCreateService =
        ApplicationClass.retrofit.create(PostCreateService::class.java)

    // photoUrl(String) 제거, 대신 imagePart / RequestBody들을 받음
    suspend fun createPost(
        imagePart: MultipartBody.Part,
        title: RequestBody,
        content: RequestBody,
        items: RequestBody
    ): Response<ApiResponse<PostCreateResponse>> = service.createPost(
        photoUrl = imagePart,
        title = title,
        content = content,
        items = items
    )
}
