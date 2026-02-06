package com.ssafy.closetory.service.user;

import com.ssafy.closetory.dto.user.AddStyleRequest;
import com.ssafy.closetory.dto.user.UpdateUserRequest;
import com.ssafy.closetory.dto.user.UserDetailResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  // 회원정보 수정
  void updateUser(
      Integer authUserId,
      Integer userId,
      UpdateUserRequest request,
      MultipartFile profilePhoto,
      MultipartFile bodyPhoto);

  // 비밀번호 확인
  void verifyPassword(Integer userId, String password);

  // 비밀번호 변경
  void changePassword(Integer userId, String newPassword, String newPasswordConfirm);

  void addStyle(Integer userId, Integer authUserId, AddStyleRequest request);

  UserDetailResponse getUserDetail(Integer authUserId, Integer userId);

  void deleteUser(Integer authUserId, Integer userId, String password);
}
