package com.ssafy.closetory.service.auth;

import com.ssafy.closetory.dto.auth.LoginRequest;
import com.ssafy.closetory.dto.auth.LoginResponse;
import com.ssafy.closetory.dto.auth.SignupRequest;

public interface AuthService {

  // 회원가입
  void signup(SignupRequest request);

  //   로그인
  LoginResponse login(LoginRequest request);

  // 로그아웃
  void logout(Integer userId);
}
