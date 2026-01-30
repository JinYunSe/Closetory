package com.ssafy.closetory.service.looks;

import com.ssafy.closetory.dto.looks.AiRecommendationRequest;
import com.ssafy.closetory.dto.looks.AiRecommendationResponse;
import com.ssafy.closetory.dto.looks.LookRegistrationRequest;
import com.ssafy.closetory.dto.looks.VirtualFittingRequest;

public interface LookService {
  String requestFitting(Integer userId, VirtualFittingRequest request);

  AiRecommendationResponse requestAiRecommendation(Integer userId, AiRecommendationRequest request);

  void lookRegistration(LookRegistrationRequest request, Integer userId);
}
