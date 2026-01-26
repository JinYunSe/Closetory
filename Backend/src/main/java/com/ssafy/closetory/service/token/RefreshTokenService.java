package com.ssafy.closetory.service.token;

public interface RefreshTokenService {

  // Refresh Token 생성
  String createRefreshToken();

  // Redis 저장
  void save(String refreshToken, Integer userId);

  // Redis 조회
  Integer getUserId(String refreshToken);

  // Redis 삭제 (로그아웃)
  void delete(String refreshToken);

  // 토큰 재발급
  void rotate(String oldRefreshToken, String newRefreshToken, Integer userId);
}
