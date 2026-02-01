package com.ssafy.closetory.dto

import java.util.Date

data class StylingResponse(
    val lookId: Int,
    val date: String?,
    val photoUrl: String,
    val topColor: String?,
    val bottomColor: String?
)
