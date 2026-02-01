package com.ssafy.closetory.service.looks;

import com.ssafy.closetory.dto.looks.*;
import java.util.List;

public interface LookService {
  String requestFitting(Integer userId, VirtualFittingRequest request);

  AiRecommendationResponse requestAiRecommendation(Integer userId, AiRecommendationRequest request);

  void lookRegistration(LookRegistrationRequest request, Integer userId);

  List<GetAllLooksResponse> getAllLooks(Integer userId);

  List<GetLooksByMonthResponse> getLooksByMonthResponse(boolean isMain, Integer userId);

  void updateLook(Integer lookId, UpdateLookRequest request, Integer userId);
}
