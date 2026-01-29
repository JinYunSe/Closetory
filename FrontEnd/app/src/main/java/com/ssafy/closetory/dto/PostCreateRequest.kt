package com.ssafy.closetory.dto

data class PostCreateRequest(
    val title: String,
    val content: String,
    val items: List<Int>?, // null 허용
)
