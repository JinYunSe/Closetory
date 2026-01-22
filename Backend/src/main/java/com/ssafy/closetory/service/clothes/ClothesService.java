package com.ssafy.closetory.service.clothes;

import com.ssafy.closetory.dto.clothes.GetClosetRequest;
import com.ssafy.closetory.dto.clothes.GetClosetResponse;
import com.ssafy.closetory.dto.clothes.GetClothesDetailResponse;

public interface ClothesService {
  GetClosetResponse getCloset(Integer userId, GetClosetRequest request);

  GetClothesDetailResponse getClothesDetail(Integer userId, Integer clothesId);
}
