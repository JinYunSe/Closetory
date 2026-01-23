package com.ssafy.closetory.dto

// 요청 형태
// data class EditProfileInfoResponse(
//    val nickname: String,
//    val gender: String, // FEMALE / MALE
//    val height: Int,
//    val weight: Int,
//    val alarmEnabled: Boolean
// )
data class EditProfileInfoResponse(
    val nickname: String,
    val gender: String, // FEMALE / MALE
    val height: Int,
    val weight: Int,
    val alarmEnabled: Boolean
)
