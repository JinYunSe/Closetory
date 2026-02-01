package com.ssafy.closetory.dto.post;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UpdateCommentResponse(Integer commentId, String content, LocalDateTime updatedAt) {}
