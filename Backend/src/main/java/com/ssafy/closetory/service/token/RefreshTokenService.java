package com.ssafy.closetory.service.token;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private static final long REFRESH_TOKEN_TTL = 7; // 7일

  private final RedisTemplate<String, String> redisTemplate;

  // Refresh Token 생성
  public String createRefreshToken() {
    return UUID.randomUUID().toString();
  }

  // Redis 저장
  public void save(Integer userId, String refreshToken) {
    redisTemplate.opsForValue().set(key(userId), refreshToken, REFRESH_TOKEN_TTL, TimeUnit.DAYS);
  }

  // Redis 조회
  public String get(Integer userId) {
    return redisTemplate.opsForValue().get(key(userId));
  }

  // Redis 삭제 (로그아웃)
  public void delete(Integer userId) {
    redisTemplate.delete(key(userId));
  }

  private String key(Integer userId) {
    return "RT:" + userId;
  }
}
