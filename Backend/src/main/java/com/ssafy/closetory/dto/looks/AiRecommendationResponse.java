package com.ssafy.closetory.dto.looks;

import java.util.List;

public record AiRecommendationResponse(String aiReason, List<LooksItem> clothesIdList) {}
