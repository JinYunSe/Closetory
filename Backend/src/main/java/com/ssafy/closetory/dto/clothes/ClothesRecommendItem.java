package com.ssafy.closetory.dto.clothes;

import com.ssafy.closetory.entity.clothes.Clothes;

public record ClothesRecommendItem(Integer clothesId, String photoUrl) {
  public static ClothesRecommendItem from(Clothes c) {
    return new ClothesRecommendItem(c.getId(), c.getPhotoUrl());
  }
}
