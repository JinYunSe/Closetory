package com.ssafy.closetory.service.looks;

import com.ssafy.closetory.dto.looks.VirtualFittingRequest;

public interface LookService {
  String requestFitting(Integer userId, VirtualFittingRequest request);
}
