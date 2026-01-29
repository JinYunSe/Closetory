package com.ssafy.closetory.dto

data class PostDetailResponse(
    val postId: Int,
    val title: String,
    val photoUrl: String,
    val content: String,
    val items: List<ItemDto> = emptyList(), // 기본적으로 빈 리스트 반환
    val createdAt: String, // 시간의 경우 String으로 받는 것이 안전.
    val views: Int,
    val likeCount: Int,
    val isLiked: Boolean
)

data class ItemDto(val clothId: Int, val photoUrl: String, val isLiked: Boolean)
