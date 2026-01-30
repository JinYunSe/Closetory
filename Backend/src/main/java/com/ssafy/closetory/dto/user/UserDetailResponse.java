package com.ssafy.closetory.dto.user;

import com.ssafy.closetory.entity.user.User;

public record UserDetailResponse(
    String nickname,
    String gender,
    Integer height,
    Integer weight,
    Boolean alarmEnabled,
    String profilePhotoUrl,
    String bodyPhotoUrl) {

  public static UserDetailResponse from(User user) {
    return new UserDetailResponse(
        user.getNickname(),
        user.getGender().name(), // FEMALE / MALE
        user.getHeight() != null ? user.getHeight().intValue() : null,
        user.getWeight() != null ? user.getWeight().intValue() : null,
        user.getAlarmEnabled(),
        user.getProfilePhotoUrl(),
        user.getBodyPhotoUrl());
  }
}
