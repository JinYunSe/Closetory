package com.ssafy.closetory.service.clothes;

import com.ssafy.closetory.dto.clothes.*;
import java.util.List;

public interface ClothesService {
  GetClosetResponse getCloset(Integer userId, GetClosetRequest request);

  GetClothesDetailResponse getClothesDetail(Integer userId, Integer clothesId);

  Integer addClothes(Integer userId, AddClothesRequest request);

  GetClothesDetailResponse updateClothes(
      Integer userId, Integer clothesId, UpdateClothesRequest request);

  void deleteClothes(Integer userId, Integer clothesId);

  String createMaskingImage(byte[] rawImage);

  GetClosetResponse getClosetForAiRecommendation(Integer userId, Boolean onlyMine);

  List<ClothesRecommendItem> getClothesRecommend(Integer clothedId, Integer userId);
}
