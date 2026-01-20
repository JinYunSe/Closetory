package com.ssafy.closetory.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String userId, @NotBlank String password) {}
