package com.ssafy.closetory.dto

import com.google.gson.annotations.SerializedName

// 룩 저장 요청 데이터
// 순서: Top, Bottom, Shoes, Outer, Accessories, Bag
data class SaveLookRequest(
    val clothesIdList: List<Int>,
    @SerializedName("aiPhotoUrl")
    val aiPhotoUrl: String,
    val aiReason: String? = null
)
