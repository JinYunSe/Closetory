package com.ssafy.closetory.dto

/**
 * 댓글 수정 API 응답
 * PATCH /api/v1/posts/{postId}/comments/{commentId}
 */
data class CommentUpdateResponse(val commentId: Long, val content: String, val updatedAt: String)
