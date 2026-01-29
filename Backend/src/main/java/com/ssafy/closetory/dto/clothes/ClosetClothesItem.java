package com.ssafy.closetory.dto.clothes;

import com.ssafy.closetory.entity.clothes.Clothes;

public record ClosetClothesItem(Integer clothesId, String photoUrl, Boolean isMine) {
  public static ClosetClothesItem of(Clothes c, boolean isMine) {
    return new ClosetClothesItem(c.getId(), c.getPhotoUrl(), isMine);
  }
}
