package com.ssafy.closetory.service.clothes;

import com.ssafy.closetory.dto.clothes.ClosetClothesItem;
import com.ssafy.closetory.dto.clothes.GetClosetRequest;
import com.ssafy.closetory.dto.clothes.GetClosetResponse;
import com.ssafy.closetory.dto.clothes.GetClothesDetailResponse;
import com.ssafy.closetory.entity.clothes.Clothes;
import com.ssafy.closetory.enums.ClothesColor;
import com.ssafy.closetory.exception.common.BadRequestException;
import com.ssafy.closetory.exception.common.NotFoundException;
import com.ssafy.closetory.repository.ClothesRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClothesServiceImpl implements ClothesService {

  private final ClothesRepository clothesRepository;

  @Override
  public GetClosetResponse getCloset(Integer userId, GetClosetRequest request) {

    boolean onlyMine = request.onlyMine() != null ? request.onlyMine() : true;

    List<Integer> tagIds = request.tagIds() != null ? request.tagIds() : List.of();
    List<Integer> seasonIds = request.seasonIds() != null ? request.seasonIds() : List.of();

    boolean tagIdsEmpty = tagIds.isEmpty();
    boolean seasonIdsEmpty = seasonIds.isEmpty();

    ClothesColor color = parseColorOrNull(request.color());

    List<Clothes> closet =
        clothesRepository.searchCloset(
            userId, onlyMine, color, seasonIds, seasonIdsEmpty, tagIds, tagIdsEmpty);

    List<ClosetClothesItem> top = new ArrayList<>();
    List<ClosetClothesItem> bottom = new ArrayList<>();
    List<ClosetClothesItem> accessories = new ArrayList<>();
    List<ClosetClothesItem> bags = new ArrayList<>();
    List<ClosetClothesItem> outer = new ArrayList<>();

    for (Clothes c : closet) {
      ClosetClothesItem item = ClosetClothesItem.from(c);

      switch (c.getClothesType()) {
        case TOP -> top.add(item);
        case BOTTOM -> bottom.add(item);
        case ACCESSORIES -> accessories.add(item);
        case BAG -> bags.add(item);
        case OUTER -> outer.add(item);
      }
    }

    return new GetClosetResponse(top, bottom, accessories, bags, outer);
  }

  @Override
  public GetClothesDetailResponse getClothesDetail(Integer userId, Integer clothesId) {
    Clothes clothes =
        clothesRepository
            .getClothesById(clothesId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 옷입니다."));
    return GetClothesDetailResponse.from(clothes, userId);
  }

  private ClothesColor parseColorOrNull(String colorStr) {
    if (colorStr == null || colorStr.isBlank()) return null;

    try {
      return ClothesColor.valueOf(colorStr.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("color 값이 올바르지 않습니다. 예: BLACK, WHITE, RED");
    }
  }
}
