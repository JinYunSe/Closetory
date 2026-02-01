package com.ssafy.closetory.dto

import java.util.Date

data class StylingResponse(
    val lookId: Int,
    val date: Date,
    val photoUrl: String,
    val topColor: String,
    val bottomColor: String
)
