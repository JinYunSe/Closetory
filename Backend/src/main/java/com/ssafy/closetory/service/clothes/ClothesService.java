package com.ssafy.closetory.service.clothes;

import com.ssafy.closetory.dto.clothes.GetClosetRequest;
import com.ssafy.closetory.dto.clothes.GetClosetResponse;

public interface ClothesService {
  GetClosetResponse getCloset(Integer userId, GetClosetRequest request);
}
