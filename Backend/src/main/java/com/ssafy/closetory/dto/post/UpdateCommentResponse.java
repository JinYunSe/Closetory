package com.ssafy.closetory.dto.post;

import lombok.Builder;

@Builder
public record UpdateCommentResponse(Integer commentId, String content) {}
