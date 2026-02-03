@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.ssafy.closetory.homeActivity.post

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.CommentCreateRequest
import com.ssafy.closetory.dto.CommentCreateResponse
import com.ssafy.closetory.dto.CommentDto
import com.ssafy.closetory.dto.CommentListResponse
import com.ssafy.closetory.dto.CommentUpdateRequest
import com.ssafy.closetory.dto.CommentUpdateResponse
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
    // Detail
    // -------------------------
    @GET("posts/{postId}")
    suspend fun getPostDetail(@Path("postId") postId: Int): ApiResponse<PostDetailResponse>

    // -------------------------
    // Like
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
    // Clothes save/unsave
    // -------------------------
    @POST("clothes/{clothesId}/save")
    suspend fun postClothesRental(@Path("clothesId") clothesId: Int): Response<ApiResponse<Unit>>

    @DELETE("clothes/{clothesId}/save")
    suspend fun deleteClothesRental(@Path("clothesId") clothesId: Int): Response<ApiResponse<Unit>>

    // -------------------------
    // Comments
    // -------------------------
    @GET("posts/{postId}/comments")
    suspend fun getComments(@Path("postId") postId: Int): ApiResponse<CommentListResponse>

    @POST("posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: Int,
        @Body request: CommentCreateRequest
    ): Response<ApiResponse<CommentCreateResponse>>

    @PATCH("posts/{postId}/comments/{commentId}")
    suspend fun updateComment(
        @Path("postId") postId: Int,
        @Path("commentId") commentId: Int,
        @Body request: CommentUpdateRequest
    ): Response<ApiResponse<CommentUpdateResponse>>

    @DELETE("posts/{postId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("postId") postId: Int,
        @Path("commentId") commentId: Int
    ): Response<ApiResponse<Unit>>
}
