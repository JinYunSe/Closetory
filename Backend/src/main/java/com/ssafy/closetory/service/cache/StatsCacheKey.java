package com.ssafy.closetory.service.cache;

import java.time.*;
import java.time.format.DateTimeFormatter;

public final class StatsCacheKey {

  private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.BASIC_ISO_DATE;
  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private StatsCacheKey() {}

  public static String date(LocalDate date) {
    return date.format(YYYYMMDD);
  }

  public static String top3(Integer userId, String date) {
    return "STATS:TOP3:" + userId + ":" + date;
  }

  public static String tagRatio(Integer userId, String date) {
    return "STATS:TAG_RATIO:" + userId + ":" + date;
  }

  public static String colorRatio(Integer userId, String date) {
    return "STATS:COLOR_RATIO:" + userId + ":" + date;
  }

  // TTL은 내일 00:00까지
  public static long ttlSecondsUntilTomorrowStart() {
    ZonedDateTime now = ZonedDateTime.now(KST);
    ZonedDateTime tomorrowStart = now.toLocalDate().plusDays(1).atStartOfDay(KST);
    long seconds = Duration.between(now, tomorrowStart).getSeconds();
    return Math.max(seconds, 60);
  }
}
