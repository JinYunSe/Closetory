package com.ssafy.closetory.service.cloth;

import com.ssafy.closetory.dto.cloth.GetClosetRequest;
import com.ssafy.closetory.dto.cloth.GetClosetResponse;

public interface ClothesService {
  GetClosetResponse getCloset(Integer userId, GetClosetRequest request);
}
