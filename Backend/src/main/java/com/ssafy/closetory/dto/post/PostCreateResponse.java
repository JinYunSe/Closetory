package com.ssafy.closetory.dto.post;

import java.util.List;

public record PostCreateResponse(
    Integer postId, String title, String photoUrl, String content, List<Integer> items) {}
