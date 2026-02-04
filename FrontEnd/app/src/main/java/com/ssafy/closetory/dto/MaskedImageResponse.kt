package com.ssafy.closetory.dto

import com.google.gson.annotations.SerializedName

data class MaskedImageResponse(
    @SerializedName("photoUrl")
    val maskedImageUrl: String
)
