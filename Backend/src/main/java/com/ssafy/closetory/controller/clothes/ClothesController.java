package com.ssafy.closetory.controller.clothes;

import com.ssafy.closetory.dto.clothes.*;
import com.ssafy.closetory.dto.common.ApiResponse;
import com.ssafy.closetory.service.clothes.ClothesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clothes")
public class ClothesController {

  private final ClothesService clothesService;

  @GetMapping
  @Operation(summary = "옷장 조회")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<GetClosetResponse>> getCloset(
      @RequestParam(required = false) List<Integer> tags,
      @RequestParam(required = false) List<Integer> seasons,
      @RequestParam(required = false) String color,
      @RequestParam(defaultValue = "true") Boolean onlyMine,
      @AuthenticationPrincipal Integer userId) {
    GetClosetRequest request = new GetClosetRequest(tags, seasons, color, onlyMine);
    GetClosetResponse response = clothesService.getCloset(userId, request);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "옷장 조회 성공", response));
  }

  @GetMapping("/{clothesId}")
  @Operation(summary = "옷 상세 정보 조회")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<GetClothesDetailResponse>> getClothesDetail(
      @PathVariable Integer clothesId, @AuthenticationPrincipal Integer userId) {
    GetClothesDetailResponse response = clothesService.getClothesDetail(userId, clothesId);
    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.ok(200, "옷 상세 정보 조회 성공", response));
  }

  @PostMapping()
  @Operation(summary = "옷 등록")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<AddClothesResponse>> addClothes(
      @Valid @RequestBody AddClothesRequest request, @AuthenticationPrincipal Integer userId) {
    Integer clothesId = clothesService.addClothes(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(201, "옷 등록 성공", new AddClothesResponse(clothesId)));
  }

  @PatchMapping(value = "/{clothesId}")
  @Operation(summary = "옷 수정")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<GetClothesDetailResponse>> updateClothes(
      @PathVariable Integer clothesId,
      @RequestBody UpdateClothesRequest request,
      @AuthenticationPrincipal Integer userId) {
    GetClothesDetailResponse response = clothesService.updateClothes(userId, clothesId, request);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "옷 수정 성공", response));
  }

  @DeleteMapping("/{clothesId}")
  @Operation(summary = "옷 삭제")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> deleteClothes(
      @PathVariable Integer clothesId, @AuthenticationPrincipal Integer userId) {
    clothesService.deleteClothes(userId, clothesId);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "옷 삭제 성공", null));
  }

  @PostMapping(value = "/masking", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "옷 누끼 따기")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Map<String, String>>> maskingImage(
      @RequestParam("clothesPhoto") MultipartFile file, @AuthenticationPrincipal Integer userId)
      throws IOException {
    String photoUrl = clothesService.createMaskingImage(file.getBytes());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(201, "옷 누끼 따기 성공", Map.of("photoUrl", photoUrl)));
  }

  @GetMapping("/{clothesId}/recommend")
  @Operation(summary = "함께 입으면 좋을 옷 추천")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<List<ClothesRecommendItem>>> getClothesRecommend(
      @PathVariable Integer clothesId, @AuthenticationPrincipal Integer userId) {
    List<ClothesRecommendItem> response = clothesService.getClothesRecommend(clothesId, userId);
    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.ok(200, "함께 입으면 좋을 옷 추천 성공", response));
  }

  @PostMapping("/{clothesId}/save")
  @Operation(summary = "다른 사람 옷 저장")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> saveClothes(
      @PathVariable Integer clothesId, @AuthenticationPrincipal Integer userId) {
    clothesService.saveClothes(clothesId, userId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(201, "다른 사람 옷 저장 성공", null));
  }

  @DeleteMapping("/{clothesId}/save")
  @Operation(summary = "다른 사람 옷 저장 취소")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> unsaveClothes(
      @PathVariable Integer clothesId, @AuthenticationPrincipal Integer userId) {
    clothesService.unsaveClothes(clothesId, userId);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "다른 사람 옷 저장 취소 성공", null));
  }

  @PostMapping("/editing")
  @Operation(summary = "옷 보정하기")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Map<String, String>>> editingImage(
    @RequestBody ClothesEditingRequest request
  ) {
    String result = clothesService.createEditingImage(request.photoUrl());
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.ok(201, "옷 보정 하기 성공", Map.of("photoUrl",result)));
  }
}
