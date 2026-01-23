package com.ssafy.closetory.dto

data class EditProfileUpdateRequest(
    val nickname: String,
    val gender: String,
    val height: Int,
    val weight: Int,
    val alarmEnabled: Boolean,
    val bodyPhotoUrl: String?,
    val profilePhotoUrl: String?
)
