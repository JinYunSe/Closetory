package com.ssafy.closetory.controller.looks;

import com.ssafy.closetory.dto.common.ApiResponse;
import com.ssafy.closetory.dto.looks.*;
import com.ssafy.closetory.service.looks.LookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
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

  @PostMapping("/ai/recommendation")
  @Operation(summary = "AI 코디 추천")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<AiRecommendationResponse>> requestAiRecommendation(
      @RequestBody AiRecommendationRequest request, @AuthenticationPrincipal Integer userId) {
    AiRecommendationResponse response = lookService.requestAiRecommendation(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(201, "ai 코디 추천 성공", response));
  }

  @PostMapping
  @Operation(summary = "룩 등록(저장소)")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> lookRegistration(
      @RequestBody LookRegistrationRequest request, @AuthenticationPrincipal Integer userId) {
    lookService.lookRegistration(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(201, "룩 등록 성공", null));
  }

  @GetMapping
  @Operation(summary = "모든 룩 조회")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<List<GetAllLooksResponse>>> getAllLooks(
      @AuthenticationPrincipal Integer userId) {
    List<GetAllLooksResponse> response = lookService.getAllLooks(userId);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "룩 조회 성공", response));
  }

  @GetMapping("/monthly")
  @Operation(summary = "월별 코디 조회")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<List<GetLooksByMonthResponse>>> getLooksByMonthResponse(
      @RequestParam boolean isMain, @AuthenticationPrincipal Integer userId) {
    List<GetLooksByMonthResponse> result = lookService.getLooksByMonthResponse(isMain, userId);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "월별 룩 정보 조회 성공", result));
  }

  @PatchMapping("/{lookId}")
  @Operation(summary = "룩 수정")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> updateLook(
      @PathVariable Integer lookId,
      @RequestBody UpdateLookRequest request,
      @AuthenticationPrincipal Integer userId) {
    lookService.updateLook(lookId, request, userId);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "룩 정보 수정 성공", null));
  }

  @DeleteMapping("/{lookId}")
  @Operation(summary = "룩 삭제")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> deleteLook(
      @PathVariable Integer lookId, @AuthenticationPrincipal Integer userId) {
    lookService.deleteLook(lookId, userId);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "룩 정보 삭제 성공", null));
  }
}
