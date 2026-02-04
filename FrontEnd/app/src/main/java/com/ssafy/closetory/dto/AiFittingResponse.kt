package com.ssafy.closetory.dto

import com.google.gson.annotations.SerializedName

data class AiFittingResponse(
    @SerializedName("aiphotoUrl")
    val aiphotoUrl: String?
)
