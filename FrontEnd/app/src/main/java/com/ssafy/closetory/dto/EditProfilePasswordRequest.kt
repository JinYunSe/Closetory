package com.ssafy.closetory.dto

data class EditProfilePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val newPasswordConfirm: String
)
