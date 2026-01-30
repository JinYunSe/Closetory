package com.ssafy.closetory.dto

data class PostEditResponse(
    val postId: Int,
    val title: String,
    val photoUrl: String,
    val content: String,
    val items: List<Int>?
)
