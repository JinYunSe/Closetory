package com.ssafy.closetory.service.user;

import com.ssafy.closetory.dto.user.ColorStatsItem;
import com.ssafy.closetory.dto.user.TagStatsItem;
import com.ssafy.closetory.dto.user.Top3Item;

import java.util.List;

public interface StatsService {
  List<Top3Item> getTop3(Integer userId, Integer authUserId);

  List<TagStatsItem> getTagStats(Integer userId, Integer authUserId);

  List<ColorStatsItem> getColorStats(Integer userId, Integer authUserId);
}
