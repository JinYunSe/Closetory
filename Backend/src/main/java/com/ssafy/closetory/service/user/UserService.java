package com.ssafy.closetory.service.user;

public interface UserService {

  // 비밀번호 확인
  void verifyPassword(Integer userId, String password);

  // 비밀번호 변경
  void changePassword(Integer userId, String newPassword, String newPasswordConfirm);
}
