package com.ssafy.closetory.dto.looks;

import com.ssafy.closetory.entity.clothes.Clothes;
import com.ssafy.closetory.enums.ClothesType;

public record LooksItem(Integer clothesId, ClothesType clothesType, String photoUrl) {
  public static LooksItem from(Clothes c) {
    return new LooksItem(c.getId(), c.getClothesType(), c.getPhotoUrl());
  }
}
