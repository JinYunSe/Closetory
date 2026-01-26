package com.ssafy.closetory.dto

// 룩 저장 응답 데이터
data class SaveLookResponse(
    val topCloth: String?,
    val bottomCloth: String?,
    val shoes: String?,
    val outerCloth: String?,
    val accessories: String?,
    val bags: String?
)
