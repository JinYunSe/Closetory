package com.ssafy.closetory.homeActivity.post

import com.ssafy.closetory.dto.ApiResponse
import com.ssafy.closetory.dto.PostDetailResponse
import com.ssafy.closetory.dto.PostEditRequest
import com.ssafy.closetory.dto.PostEditResponse
import com.ssafy.closetory.dto.PostItemResponse
import com.ssafy.closetory.dto.PostQueryFilter
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

// 게시판 관련 API 정의
interface PostService {

    // 게시글 목록 조회
    @GET("posts")
    suspend fun getPosts(
        @Query("keyword") keyword: String? = null,
        @Query("searchfilter") searchfilter: PostQueryFilter? = null
    ): Response<ApiResponse<List<PostItemResponse>>>

    // 게시글 상세 페이지 조회
    @GET("posts/{postId}")
    suspend fun getPostDetail(@Path("postId") postId: Int): Response<ApiResponse<PostDetailResponse>>

    // 게시글 수정
    @Multipart
    @PATCH("posts/{postId}")
    suspend fun editPost(
        @Path("postId") postId: Int,
        @Part photo: MultipartBody.Part?, // 변경 안 했으면 null
        @Part("request") request: PostEditRequest
    ): Response<ApiResponse<PostEditResponse>>

    // 게시글 삭제
    @DELETE("posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: Int): Response<ApiResponse<Unit>>
}
