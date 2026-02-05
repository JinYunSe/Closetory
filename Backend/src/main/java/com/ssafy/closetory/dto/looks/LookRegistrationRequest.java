package com.ssafy.closetory.dto.looks;

import java.util.List;

public record LookRegistrationRequest(
  List<Integer> clothesIdList, String aiPhotoUrl, String aiReason) {}
