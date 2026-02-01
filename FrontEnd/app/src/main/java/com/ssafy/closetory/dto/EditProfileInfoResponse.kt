package com.ssafy.closetory.dto

data class EditProfileInfoResponse(
    val nickname: String,
    val gender: String, // FEMALE / MALE
    val height: Int,
    val weight: Int,
    val alarmEnabled: Boolean,
    val profilePhotoUrl: String?, // 프로필 사진
    val bodyPhotoUrl: String? // 전신 사진
)
