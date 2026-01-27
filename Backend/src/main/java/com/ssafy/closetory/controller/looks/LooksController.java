package com.ssafy.closetory.controller.looks;

import com.ssafy.closetory.dto.common.ApiResponse;
import com.ssafy.closetory.dto.looks.VirtualFittingRequest;
import com.ssafy.closetory.service.looks.LookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/looks")
public class LooksController {
  private final LookService lookService;

  @PostMapping("/ai/fitting")
  @Operation(summary = "가상 피팅")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Map<String, String>>> requestFitting(
      @RequestBody VirtualFittingRequest request, @AuthenticationPrincipal Integer userId) {
    String aiImageUrl = lookService.requestFitting(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(201, "가상 피팅 성공", Map.of("aiImageUrl", aiImageUrl)));
  }
}
