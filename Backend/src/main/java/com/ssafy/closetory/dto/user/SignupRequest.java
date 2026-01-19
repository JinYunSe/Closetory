package com.ssafy.closetory.dto.user;

import com.ssafy.closetory.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

  @NotBlank private String userId;

  @NotBlank private String password;

  @NotBlank private String passwordConfirm;

  @NotBlank private String nickname;

  @NotNull private Gender gender;

  @Min(100)
  @Max(250)
  private Integer height;

  @Min(20)
  @Max(200)
  private Integer weight;
}
