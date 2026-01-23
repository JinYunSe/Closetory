package com.ssafy.closetory.dto

// 비밀번호 변경 Response DTO

data class EditProfileBaseResponse(
    val httpStatusCode: Int,
    val responseMessage: String?, // 성공
    val errorMessage: String? // 실패
)
