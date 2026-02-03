package com.ssafy.closetory.homeActivity.post

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.PostCreateResponse
import com.ssafy.closetory.dto.PostDetailResponse
import com.ssafy.closetory.dto.PostEditResponse
import com.ssafy.closetory.dto.PostItemResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface PostService {

    // -------------------------
    // List
    // -------------------------
    @GET("posts")
    suspend fun getPosts(
        @Query("keyword") keyword: String,
        @Query("searchfilter") searchFilter: String
    ): ApiResponse<List<PostItemResponse>>

    @GET("posts")
    suspend fun getPostsFilter(@Query("searchfilter") searchFilter: String): ApiResponse<List<PostItemResponse>>

    // -------------------------
    // Detail (주의: 서버가 여기서 views +1 한다면, 불필요한 재호출 금지)
    // -------------------------
    @GET("posts/{postId}")
    suspend fun getPostDetail(@Path("postId") postId: Int): ApiResponse<PostDetailResponse>

    // -------------------------
    // Like (✅ 상세 재조회 없이 이 API로만 좋아요 상태 갱신)
    // 서버 스펙에 맞게 경로/메서드 조정 필요할 수 있음
    // -------------------------
    @POST("posts/{postId}/like")
    suspend fun likePost(@Path("postId") postId: Int): ApiResponse<Unit>

    @DELETE("posts/{postId}/like")
    suspend fun unlikePost(@Path("postId") postId: Int): ApiResponse<Unit>

    // -------------------------
    // Create / Update / Delete
    // -------------------------
    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Part photo: MultipartBody.Part,
        @Part("request") request: RequestBody
    ): Response<ApiResponse<PostCreateResponse>>

    @Multipart
    @PATCH("posts/{postId}")
    suspend fun editPost(
        @Path("postId") postId: Int,
        @Part photo: MultipartBody.Part?,
        @Part("request") request: RequestBody
    ): ApiResponse<PostEditResponse>

    @DELETE("posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: Int): ApiResponse<Unit>

    // -------------------------
    // (선택) Clothes save/unsave
    // -------------------------
    @POST("clothes/{clothesId}/save")
    suspend fun postClothesRental(@Path("clothesId") clothesId: Int): Response<ApiResponse<Unit>>

    @DELETE("clothes/{clothesId}/save")
    suspend fun deleteClothesRental(@Path("clothesId") clothesId: Int): Response<ApiResponse<Unit>>
}
