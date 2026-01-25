package com.ssafy.closetory.service.token;

public interface RefreshTokenService {

  // Refresh Token 생성
  String createRefreshToken();

  // Redis 저장
  void save(Integer userId, String refreshToken);

  // Redis 조회
  String get(Integer userId);

  // Redis 삭제 (로그아웃)
  void delete(Integer userId);
}
