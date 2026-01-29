package com.ssafy.closetory.dto.user;

import com.ssafy.closetory.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @Size(min = 1, max = 20) String nickname,
    Gender gender,
    @Min(100) @Max(250) Integer height,
    @Min(20) @Max(200) Integer weight,
    Boolean alarmEnabled) {}
