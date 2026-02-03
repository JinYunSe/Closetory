package com.ssafy.closetory.dto

import com.google.gson.annotations.SerializedName

/**
 * 댓글 목록 조회 API 응답
 * GET /api/v1/posts/{postId}/comments
 */
data class CommentListResponse(val postId: String, val comment: List<CommentDto>)
