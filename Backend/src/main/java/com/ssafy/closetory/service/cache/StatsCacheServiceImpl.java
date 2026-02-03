package com.ssafy.closetory.service.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatsCacheServiceImpl implements StatsCacheService {

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public <T> T getOrLoad(
      String key, long ttlSeconds, TypeReference<T> typeRef, Supplier<T> loader) {
    String cached = redisTemplate.opsForValue().get(key);
    if (cached != null) {
      try {
        return objectMapper.readValue(cached, typeRef);
      } catch (Exception ignore) {
        redisTemplate.delete(key);
      }
    }

    T loaded = loader.get();

    try {
      String json = objectMapper.writeValueAsString(loaded);
      redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
    } catch (Exception ignore) {
      // 캐시 저장 실패해도 본 응답은 정상 반환
    }

    return loaded;
  }
}
