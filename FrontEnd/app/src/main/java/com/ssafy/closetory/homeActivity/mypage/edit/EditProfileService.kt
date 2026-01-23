// // EditProfileService
//
// package com.ssafy.closetory.homeActivity.mypage.edit
//
// import com.ssafy.closetory.dto.UpdateUserProfileRequest
// import retrofit2.http.*
//
// interface EditProfileService {
//
//    // 회원 정보 조회
//    @GET("users/{userId}")
//    suspend fun getUserProfile(@Header("Authorization") token: String, @Path("userId") userId: Int): UserProfileResponse
//
//    // 회원 정보 수정
//    @PATCH("users/{userId}")
//    suspend fun updateUserProfile(
//        @Header("Authorization") token: String,
//        @Path("userId") userId: Int,
//        @Body request: UpdateUserProfileRequest
//    )
// }
