package com.ssafy.closetory.dto.post;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(@NotBlank(message = "댓글 내용은 비워둘 수 없습니다.") String content) {}
