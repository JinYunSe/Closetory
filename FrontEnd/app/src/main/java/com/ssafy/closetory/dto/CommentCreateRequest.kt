package com.ssafy.closetory.dto

/**
 * 댓글 등록 API 요청
 * POST /api/v1/posts/{postId}/comments
 */
data class CommentCreateRequest(val content: String)
