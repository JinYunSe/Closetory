package com.ssafy.closetory.dto

// 요청 형태
// data class EditProfileInfoResponse(
//    val nickname: String,
//    val gender: String, // FEMALE / MALE
//    val height: Int,
//    val weight: Int,
//    val alarmEnabled: Boolean,
//    val profilePhotoUrl: String?, // 프로필 사진
//    val bodyPhotoUrl: String? // 전신 사진
// )
data class EditProfileInfoResponse(
    val nickname: String,
    val gender: String, // FEMALE / MALE
    val height: Int,
    val weight: Int,
    val alarmEnabled: Boolean,
    val profilePhotoUrl: String?, // 프로필 사진
    val bodyPhotoUrl: String? // 전신 사진
)
