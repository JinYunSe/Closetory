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
  public ResponseEntity<ApiResponse<Void>> addClothes(
      @Valid @RequestBody AddClothesRequest request, @AuthenticationPrincipal Integer userId) {
    clothesService.addClothes(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(201, "옷 등록 성공", null));
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
    String maskedImageUrl = clothesService.createMaskingImage(file.getBytes());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(201, "옷 누끼 따기 성공", Map.of("maskedImageUrl", maskedImageUrl)));
  }
}
