package com.ssafy.closetory.dto.post;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PostCreateRequest(
    @NotBlank String title,
    @NotBlank String photoUrl,
    @NotBlank String content,
    List<Integer> items) {}
