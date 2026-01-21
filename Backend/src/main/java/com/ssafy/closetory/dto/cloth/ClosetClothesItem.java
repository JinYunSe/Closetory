package com.ssafy.closetory.dto.cloth;

import com.ssafy.closetory.entity.clothes.Clothes;

public record ClosetClothesItem(Integer clothesId, String photoUrl) {
  public static ClosetClothesItem from(Clothes c) {
    return new ClosetClothesItem(c.getId(), c.getPhotoUrl());
  }
}
