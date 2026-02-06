package com.ssafy.closetory.dto.post;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CreateCommentResponse(Integer commentId, String content, LocalDateTime createdAt) {}
