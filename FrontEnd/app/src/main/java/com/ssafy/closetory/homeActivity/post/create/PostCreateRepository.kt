package com.ssafy.closetory.homeActivity.post.create

import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.PostCreateRequest
import com.ssafy.closetory.dto.PostCreateResponse
import okhttp3.MultipartBody
import retrofit2.Response

class PostCreateRepository {

    private val service: PostCreateService =
        ApplicationClass.retrofit.create(PostCreateService::class.java)

    suspend fun createPost(
        photo: MultipartBody.Part,
        request: PostCreateRequest
    ): Response<ApiResponse<PostCreateResponse>> = service.createPost(
        photo = photo,
        request = request
    )
}
