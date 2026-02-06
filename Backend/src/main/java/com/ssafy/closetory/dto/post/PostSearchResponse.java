package com.ssafy.closetory.dto.post;

import lombok.Builder;

@Builder
public record PostSearchResponse(
    Integer postId,
    String title,
    String photoUrl,
    Integer views,
    Integer likes,
    Integer comments,
    String nickname) {}
