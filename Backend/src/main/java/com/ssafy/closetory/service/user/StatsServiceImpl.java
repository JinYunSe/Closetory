package com.ssafy.closetory.service.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ssafy.closetory.dto.user.ColorStatsItem;
import com.ssafy.closetory.dto.user.TagStatsItem;
import com.ssafy.closetory.dto.user.Top3Item;
import com.ssafy.closetory.exception.common.ForbiddenException;
import com.ssafy.closetory.repository.LookRepository;
import com.ssafy.closetory.repository.projection.StatsRow;
import com.ssafy.closetory.repository.projection.Top3Row;
import com.ssafy.closetory.service.cache.StatsCacheKey;
import com.ssafy.closetory.service.cache.StatsCacheService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

  private final LookRepository lookRepository;
  private final StatsCacheService statsCacheService;

  @Override
  public List<Top3Item> getTop3(Integer userId, Integer authUserId) {
    if (!userId.equals(authUserId)) {
      throw new ForbiddenException("자신의 통계만 볼 수 있습니다.");
    }

    LocalDate today = LocalDate.now();
    String date = StatsCacheKey.date(today);
    String key = StatsCacheKey.top3(userId, date);
    long ttl = StatsCacheKey.ttlSecondsUntilTomorrowStart();

    return statsCacheService.getOrLoad(
        key,
        ttl,
        new TypeReference<List<Top3Item>>() {},
        () -> {
          DateRange range = rolling30Range(today);
          List<Top3Row> rows = lookRepository.findTop3Between(userId, range.start(), range.end());
          int[] rank = {1};
          return rows.stream()
              .map(
                  r ->
                      new Top3Item(r.getClothesId(), r.getPhotoUrl(), rank[0]++, r.getUsageCount()))
              .toList();
        });
  }

  @Override
  public List<TagStatsItem> getTagStats(Integer userId, Integer authUserId) {
    if (!userId.equals(authUserId)) {
      throw new ForbiddenException("자신의 통계만 볼 수 있습니다.");
    }

    LocalDate today = LocalDate.now();
    String date = StatsCacheKey.date(today);
    String key = StatsCacheKey.tagRatio(userId, date);
    long ttl = StatsCacheKey.ttlSecondsUntilTomorrowStart();

    return statsCacheService.getOrLoad(
        key,
        ttl,
        new TypeReference<List<TagStatsItem>>() {},
        () -> {
          DateRange range = rolling30Range(today);
          List<StatsRow> rows =
              lookRepository.findTagStatsBetween(userId, range.start(), range.end());
          return rows.stream().map(r -> new TagStatsItem(r.getName(), r.getPercentage())).toList();
        });
  }

  @Override
  public List<ColorStatsItem> getColorStats(Integer userId, Integer authUserId) {
    if (!userId.equals(authUserId)) {
      throw new ForbiddenException("자신의 통계만 볼 수 있습니다.");
    }

    LocalDate today = LocalDate.now();
    String date = StatsCacheKey.date(today);
    String key = StatsCacheKey.colorRatio(userId, date);
    long ttl = StatsCacheKey.ttlSecondsUntilTomorrowStart();

    return statsCacheService.getOrLoad(
        key,
        ttl,
        new TypeReference<List<ColorStatsItem>>() {},
        () -> {
          DateRange range = rolling30Range(today);
          List<StatsRow> rows =
              lookRepository.findColorStatsBetween(userId, range.start(), range.end());
          return rows.stream()
              .map(r -> new ColorStatsItem(r.getName(), r.getPercentage()))
              .toList();
        });
  }

  private DateRange rolling30Range(LocalDate today) {
    LocalDate start = today.minusDays(29);
    LocalDate end = today.plusDays(1);
    return new DateRange(start, end);
  }

  private record DateRange(LocalDate start, LocalDate end) {}
}
