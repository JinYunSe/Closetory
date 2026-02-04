package com.ssafy.closetory.dto

/**
 * 댓글 등록 API 응답
 * POST /api/v1/posts/{postId}/comments
 */
data class CommentCreateResponse(val commentId: Long, val content: String, val createdAt: String)
