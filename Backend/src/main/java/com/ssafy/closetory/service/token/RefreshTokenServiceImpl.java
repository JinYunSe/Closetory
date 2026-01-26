package com.ssafy.closetory.service.token;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private static final long REFRESH_TOKEN_TTL = 7; // 7일

  private final RedisTemplate<String, String> redisTemplate;

  // Refresh Token 생성
  @Override
  public String createRefreshToken() {
    return UUID.randomUUID().toString();
  }

  // 로그인
  @Override
  public void save(String refreshToken, Integer userId) {
    redisTemplate
        .opsForValue()
        .set(key(refreshToken), userId.toString(), REFRESH_TOKEN_TTL, TimeUnit.DAYS);
  }

  @Override
  public Integer getUserId(String refreshToken) {
    String userId = redisTemplate.opsForValue().get(key(refreshToken));
    return userId != null ? Integer.valueOf(userId) : null;
  }

  // 토큰 재발급
  @Override
  public void rotate(String oldRefreshToken, String newRefreshToken, Integer userId) {
    redisTemplate.delete(key(oldRefreshToken));
    save(newRefreshToken, userId);
  }

  // 로그아웃
  @Override
  public void delete(String refreshToken) {
    redisTemplate.delete(key(refreshToken));
  }

  private String key(String refreshToken) {
    return "RT:" + refreshToken;
  }
}
