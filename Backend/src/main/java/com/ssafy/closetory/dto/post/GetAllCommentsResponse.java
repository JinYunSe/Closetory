package com.ssafy.closetory.dto.post;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record GetAllCommentsResponse(
    Integer commentId,
    String nickname,
    String content,
    String profileImage,
    LocalDateTime createdAt) {}
