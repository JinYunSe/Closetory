package com.ssafy.closetory.service.looks;

import com.ssafy.closetory.dto.looks.VirtualFittingRequest;
import com.ssafy.closetory.dto.looks.VirtualFittingResponse;

public interface LookService {
  VirtualFittingResponse requestFitting(Integer userId, VirtualFittingRequest request);
}
