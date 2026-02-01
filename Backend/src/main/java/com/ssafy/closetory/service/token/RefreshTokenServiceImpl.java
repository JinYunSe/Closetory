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
  private static final String RT_PREFIX = "RT:";
  private static final String RT_USER_PREFIX = "RT:USER:";

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

    redisTemplate
        .opsForValue()
        .set(RT_USER_PREFIX + userId, refreshToken, REFRESH_TOKEN_TTL, TimeUnit.DAYS);
  }

  @Override
  public Integer getUserId(String refreshToken) {
    String userId = redisTemplate.opsForValue().get(key(refreshToken));
    return userId != null ? Integer.valueOf(userId) : null;
  }

  // 토큰 재발급
  @Override
  public void rotate(String oldRefreshToken, String newRefreshToken, Integer userId) {
    redisTemplate.delete(RT_PREFIX + oldRefreshToken);
    save(newRefreshToken, userId);
  }

  // 로그아웃
  @Override
  public void delete(String refreshToken) {
    Integer userId = getUserId(refreshToken);

    if (userId != null) {
      redisTemplate.delete(RT_USER_PREFIX + userId);
    }
    redisTemplate.delete(RT_PREFIX + refreshToken);
  }

  @Override
  public void deleteByUserId(Integer userId) {
    String userKey = RT_USER_PREFIX + userId;
    String refreshToken = redisTemplate.opsForValue().get(userKey);

    if (refreshToken != null) {
      redisTemplate.delete(RT_PREFIX + refreshToken);
    }
    redisTemplate.delete(userKey);
  }

  private String key(String refreshToken) {
    return RT_PREFIX + refreshToken;
  }
}
