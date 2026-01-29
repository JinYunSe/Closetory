package com.ssafy.closetory.dto

data class EditProfileUpdateData(
    val nickname: String,
    val gender: String,
    val height: Int,
    val weight: Int,
    val alarmEnabled: Boolean
)
