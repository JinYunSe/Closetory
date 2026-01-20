package com.ssafy.closetory.dto.auth;

import com.ssafy.closetory.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignupRequest(
    @NotBlank String userId,
    @NotBlank String password,
    @NotBlank String passwordConfirm,
    @NotBlank String nickname,
    @NotNull Gender gender,
    @Min(100) @Max(250) Integer height,
    @Min(20) @Max(200) Integer weight) {}
