package com.ssafy.closetory.service.user;

import com.ssafy.closetory.dto.user.Top3Item;
import java.util.List;

public interface StatsService {
  List<Top3Item> getTop3(Integer userId, Integer authUserId);
}
