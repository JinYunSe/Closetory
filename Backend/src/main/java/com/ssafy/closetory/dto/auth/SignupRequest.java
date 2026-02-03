package com.ssafy.closetory.dto.auth;

import com.ssafy.closetory.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record SignupRequest(
  @NotBlank(message = "아이디는 필수입니다.")
  @Pattern(
    regexp = "^[A-Za-z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]+$",
    message = "아이디는 영문 대소문자, 숫자, 특수문자만 사용할 수 있습니다."
  )
  String username, // 아이디
  @NotBlank@NotBlank(message = "비밀번호는 필수입니다.")
  @Pattern(
    regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
    message = "비밀번호는 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다."
  )
  String password, // 비밀번호
  @NotBlank String passwordConfirm, // 비밀번호 확인
  @NotBlank
  @Size(max = 10, message = "닉네임은 최대 10자까지 가능합니다.")
  @Pattern(
    regexp = "^[A-Za-z0-9가-힣]+$",
    message = "닉네임에는 특수문자를 사용할 수 없습니다."
  )
  String nickname, // 닉네임
  @NotNull Gender gender, // 성별
  @Min(100) @Max(250) Short height, // 키(cm)
  @Min(20) @Max(200) Short weight, // 몸무게(kg)
  @NotNull Boolean alarmEnabled // 기본 false 처리
) {}
