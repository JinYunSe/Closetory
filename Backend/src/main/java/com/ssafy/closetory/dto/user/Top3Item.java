package com.ssafy.closetory.dto.user;

import com.ssafy.closetory.entity.clothes.Clothes;

public record Top3Item(Integer clothesId, String photoUrl, Integer rank, Integer usageCount) {
  public static Top3Item from(Clothes c, Integer rank, Integer usageCount) {
    return new Top3Item(c.getId(), c.getPhotoUrl(), rank, usageCount);
  }
}
