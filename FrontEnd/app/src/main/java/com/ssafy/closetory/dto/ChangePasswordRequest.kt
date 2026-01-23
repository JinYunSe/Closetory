package com.ssafy.closetory.dto

// 요청 형태
// {
//    "loginId" : "ssafy123",
//    "password" : "ssafy123!",
//    "nickname" : "김싸피123",
// }
data class ChangePasswordRequest(
    val username: String,
    val password: String,
    val passwordConfirm: String,
    val nickname: String,
    val gender: String, // "Male" or "Female"
    val height: Int, // cm
    val weight: Int, // kg
    val alarmEnabled: Boolean
)
