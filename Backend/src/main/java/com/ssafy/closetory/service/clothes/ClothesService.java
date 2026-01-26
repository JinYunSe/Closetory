package com.ssafy.closetory.service.clothes;

import com.ssafy.closetory.dto.clothes.*;
import org.springframework.web.multipart.MultipartFile;

public interface ClothesService {
  GetClosetResponse getCloset(Integer userId, GetClosetRequest request);

  GetClothesDetailResponse getClothesDetail(Integer userId, Integer clothesId);

  void addClothes(Integer userId, AddClothesRequest request, MultipartFile photo);

  GetClothesDetailResponse updateClothes(
      Integer userId, Integer clothesId, UpdateClothesRequest request, MultipartFile photo);

  void deleteClothes(Integer userId, Integer clothesId);
}
