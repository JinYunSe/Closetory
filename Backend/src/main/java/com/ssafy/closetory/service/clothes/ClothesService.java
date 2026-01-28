package com.ssafy.closetory.service.clothes;

import com.ssafy.closetory.dto.clothes.*;

public interface ClothesService {
  GetClosetResponse getCloset(Integer userId, GetClosetRequest request);

  GetClothesDetailResponse getClothesDetail(Integer userId, Integer clothesId);

  Integer addClothes(Integer userId, AddClothesRequest request);

  GetClothesDetailResponse updateClothes(
      Integer userId, Integer clothesId, UpdateClothesRequest request);

  void deleteClothes(Integer userId, Integer clothesId);

  String createMaskingImage(byte[] rawImage);
}
