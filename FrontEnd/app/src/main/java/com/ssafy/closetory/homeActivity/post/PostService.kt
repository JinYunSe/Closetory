package com.ssafy.closetory.homeActivity.post

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.PostCreateResponse
import com.ssafy.closetory.dto.PostDetailResponse
import com.ssafy.closetory.dto.PostEditResponse
import com.ssafy.closetory.dto.PostItemResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PostService {

    // -------------------------
    // Read - List
    // -------------------------
    @GET("posts")
    suspend fun getPosts(
        @Query("keyword") keyword: String? = null,
        @Query("filter") filter: String
    ): ApiResponse<List<PostItemResponse>>

    // -------------------------
    // Read - List (필터만)
    // -------------------------
    @GET("posts")
    suspend fun getPostsFilter(
        // ✅ 노션에 있던 "getPostsFilter" 용도
        @Query("searchfilter") searchFilter: String
    ): ApiResponse<List<PostItemResponse>>

    // -------------------------
    // Read - Detail
    // -------------------------
    @GET("posts/{postId}")
    suspend fun getPostDetail(@Path("postId") postId: Int): ApiResponse<PostDetailResponse>

    // -------------------------
    // Create (Multipart: photo + request JSON)
    // -------------------------
    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Part photo: MultipartBody.Part,
        @Part("request") request: RequestBody
    ): Response<ApiResponse<PostCreateResponse>>

    // -------------------------
    // Update (Multipart: photo optional + request JSON)
    // -------------------------
    @Multipart
    @PATCH("posts/{postId}")
    suspend fun editPost(
        @Path("postId") postId: Int,
        @Part photo: MultipartBody.Part?, // 사진 변경 없으면 null
        @Part("request") request: RequestBody
    ): ApiResponse<PostEditResponse>

    // -------------------------
    // Delete
    // -------------------------
    @DELETE("posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: Int): ApiResponse<Unit>

    // -------------------------
    // (예시) 옷 저장/해제 - 너희 기존 API에 맞춰 경로/메서드 수정
    // -------------------------
    @POST("clothes/{clothesId}/save")
    suspend fun postClothesRental(@Path("clothesId") clothesId: Int): Response<ApiResponse<Unit>>

    @DELETE("clothes/{clothesId}/save")
    suspend fun deleteClothesRental(@Path("clothesId") clothesId: Int): Response<ApiResponse<Unit>>
}
