package com.ssafy.closetory.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserDeleteRequest(@NotBlank String password) {}
