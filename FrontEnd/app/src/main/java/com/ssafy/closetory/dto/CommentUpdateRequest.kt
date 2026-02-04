package com.ssafy.closetory.dto

/**
 * 댓글 수정 API 요청
 * PATCH /api/v1/posts/{postId}/comments/{commentId}
 */
data class CommentUpdateRequest(val content: String)
