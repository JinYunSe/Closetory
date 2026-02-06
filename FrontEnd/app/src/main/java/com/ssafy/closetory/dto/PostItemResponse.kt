package com.ssafy.closetory.dto

// 게시글 목록 카드 1개에 필요한 응답 데이터
data class PostItemResponse(
    val postId: Int,
    val title: String,
    val photoUrl: String,
    val views: Int,
    val likes: Int,
    val comments: Int,
    val nickname: String
)
