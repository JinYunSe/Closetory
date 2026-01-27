package com.ssafy.closetory.dto

data class PostCreateRequest(
    val title: String,
    val photoUrl: String, // 필수
    val items: List<Int>?, // null 허용
    val content: String
)
