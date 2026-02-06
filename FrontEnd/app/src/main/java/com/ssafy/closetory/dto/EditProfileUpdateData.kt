package com.ssafy.closetory.dto

data class EditProfileUpdateData(
    val nickname: String,
    val gender: String,
    val height: Short,
    val weight: Short,
    val alarmEnabled: Boolean
)
