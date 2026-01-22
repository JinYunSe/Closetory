package com.ssafy.closetory.dto.clothes;

import com.ssafy.closetory.entity.clothes.Clothes;
import com.ssafy.closetory.entity.clothes.Season;
import com.ssafy.closetory.entity.clothes.Tag;
import com.ssafy.closetory.enums.ClothesColor;
import com.ssafy.closetory.enums.ClothesType;
import java.util.List;

public record GetClothesDetailResponse(
    Integer clothesId,
    String photoUrl,
    List<String> tags,
    ClothesType clothesType,
    List<String> seasons,
    ClothesColor color,
    Boolean isMine) {
  public static GetClothesDetailResponse from(Clothes clothes, Integer userId) {
    List<String> tags = clothes.getTags().stream().map(Tag::getTagName).toList();
    List<String> seasons = clothes.getSeasons().stream().map(Season::getSeasonName).toList();
    boolean isMine = userId.equals(clothes.getOwner().getId());

    return new GetClothesDetailResponse(
        clothes.getId(),
        clothes.getPhotoUrl(),
        tags,
        clothes.getClothesType(),
        seasons,
        clothes.getColor(),
        isMine);
  }
}
