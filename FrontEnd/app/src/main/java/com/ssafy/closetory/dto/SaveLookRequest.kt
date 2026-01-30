package com.ssafy.closetory.dto

// 룩 저장 요청 데이터
// 순서: Top, Bottom, Shoes, Outer, Accessory, Bag
data class SaveLookRequest(val clothesIdList: List<Int>, val aiImageUrl: String)
