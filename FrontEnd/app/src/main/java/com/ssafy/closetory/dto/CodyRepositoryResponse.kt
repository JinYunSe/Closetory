package com.ssafy.closetory.dto

// 현재 코드
data class CodyRepositoryResponse(
    val lookId: Int,
    val photoUrl: String,
    val date: String,
    val aiReason: String?,
    val onlyMine: Boolean
)
