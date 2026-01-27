package com.ssafy.closetory.dto

data class CodyRepositoryResponse(
    val lookId: Int,
    val photoUrl: String,
    val date: String,
    val reason: String?,
    val onlyMine: Boolean
)
