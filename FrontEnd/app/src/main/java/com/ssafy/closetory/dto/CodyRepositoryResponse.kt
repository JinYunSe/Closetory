package com.ssafy.closetory.dto

import com.google.gson.annotations.SerializedName

// 현재 코드
data class CodyRepositoryResponse(
    val lookId: Int,
    val photoUrl: String,
    val date: String?,
    @SerializedName(value = "aiReason", alternate = ["reason"])
    val aiReason: String?,
    val onlyMine: Boolean
)
