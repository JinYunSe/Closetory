package com.ssafy.closetory.dto.auth;

import com.ssafy.closetory.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SignupRequest(
    @NotBlank String username, // 아이디
    @NotBlank String password, // 비밀번호
    @NotBlank String passwordConfirm, // 비밀번호 확인
    @NotBlank String nickname, // 닉네임
    @NotNull Gender gender, // 성별
    Short height, // 키(cm)
    Short weight, // 몸무게(kg)
    @NotNull Boolean alarmEnabled // 기본 false 처리
    ) {}
