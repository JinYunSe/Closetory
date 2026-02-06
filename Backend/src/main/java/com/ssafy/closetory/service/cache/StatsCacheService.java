package com.ssafy.closetory.service.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.function.Supplier;

public interface StatsCacheService {
  <T> T getOrLoad(String key, long ttlSeconds, TypeReference<T> typeRef, Supplier<T> loader);

  void evictToday(Integer userId);
}
