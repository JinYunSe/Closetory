package com.ssafy.closetory.dto.post;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
    Integer postId,
    String title,
    String photoUrl,
    String content,
    List<PostItemResponse> items,
    LocalDateTime createdAt,
    Integer views,
    Integer likeCount,
    Boolean isLiked,
    Integer userId,
    String nickname,
    String profilePhotoUrl) {}
