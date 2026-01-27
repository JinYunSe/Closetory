package com.ssafy.closetory.dto

data class PostCreateResponse(
    val postId: Int,
    val title: String,
    val photoUrl: String,
    val items: List<Int>?,
    val content: String
)
