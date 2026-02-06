package com.ssafy.closetory.service.cache;

import java.time.*;
import java.time.format.DateTimeFormatter;

public final class StatsCacheKey {

  private static final DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");
  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private StatsCacheKey() {}

  public static String yyyyMM(LocalDate date) {
    return date.format(YYYYMM);
  }

  public static String top3(Integer userId, String yyyyMM) {
    return "STATS:TOP3:" + userId + ":" + yyyyMM;
  }

  public static String tagRatio(Integer userId, String yyyyMM) {
    return "STATS:TAG_RATIO:" + userId + ":" + yyyyMM;
  }

  public static String colorRatio(Integer userId, String yyyyMM) {
    return "STATS:COLOR_RATIO:" + userId + ":" + yyyyMM;
  }

  // TTL은 다음달 1일 00:00까지 남은 시간으로
  public static long ttlSecondsUntilNextMonthStart() {
    ZonedDateTime now = ZonedDateTime.now(KST);
    ZonedDateTime nextMonthStart =
        now.withDayOfMonth(1).plusMonths(1).toLocalDate().atStartOfDay(KST);

    long seconds = Duration.between(now, nextMonthStart).getSeconds();
    return Math.max(seconds, 60);
  }
}
