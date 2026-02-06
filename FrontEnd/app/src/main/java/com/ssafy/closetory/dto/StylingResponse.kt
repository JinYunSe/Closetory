package com.ssafy.closetory.dto

data class StylingResponse(
    val lookId: Int?,
    val date: String, // 서버에서 내려주는 날짜 (예: 2026-02-01 또는 2026-02-01T00:00:00)
    val photoUrl: String, // 룩 이미지 URL
    val topColor: String? = null,
    val bottomColor: String? = null
)
