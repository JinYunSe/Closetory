 package com.ssafy.closetory.homeActivity.post
//
// import com.ssafy.closetory.ApplicationClass
// import com.ssafy.closetory.dto.ApiResponse
// import com.ssafy.closetory.dto.PostItemResponse
// import com.ssafy.closetory.dto.PostQueryFilter
//
// 게시글 관련 Repository (목록/상세/작성 등에서 재사용 가능한 형태)
class PostRepository {
//
//    // Retrofit Service 생성
//    private val postService: PostService =
//        ApplicationClass.retrofit.create(PostService::class.java)
//
//    // 게시글 목록/검색 조회
//    suspend fun getPosts(keyword: String?, filter: PostQueryFilter): ApiResponse<List<PostItemResponse>> = try {
//        val res = postService.getPosts(
//            keyword = keyword,
//            filter = filter
//        )
//
//        if (res.isSuccessful) {
//            // 성공 응답 (ApiResponse<List<PostItemResponse>>)
//            res.body() ?: ApiResponse(
//                httpStatusCode = res.code(),
//                responseMessage = null,
//                errorMessage = "응답 바디가 비어있습니다.",
//                data = null
//            )
//        } else {
//            // 실패 응답 (서버가 errorBody를 ApiResponse 형태로 안 줄 수도 있어서 raw로 저장)
//            val rawError = res.errorBody()?.string()
//            ApiResponse(
//                httpStatusCode = res.code(),
//                responseMessage = null,
//                errorMessage = rawError ?: "서버 오류가 발생했습니다.",
//                data = null
//            )
//        }
//    } catch (e: Exception) {
//        // 네트워크 예외 등
//        ApiResponse(
//            httpStatusCode = -1,
//            responseMessage = null,
//            errorMessage = e.message ?: "네트워크 오류가 발생했습니다.",
//            data = null
//        )
//    }
 }
