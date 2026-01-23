package com.ssafy.closetory.controller.clothes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.closetory.dto.clothes.AddClothesRequest;
import com.ssafy.closetory.dto.clothes.GetClosetRequest;
import com.ssafy.closetory.dto.clothes.GetClosetResponse;
import com.ssafy.closetory.dto.clothes.GetClothesDetailResponse;
import com.ssafy.closetory.dto.common.ApiResponse;
import com.ssafy.closetory.service.clothes.ClothesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
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

  @PostMapping(
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "옷 등록")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> addClothes(
      @RequestPart("photo") MultipartFile photo,
      @RequestPart("request") String requestJson,
      @AuthenticationPrincipal Integer userId)
      throws JsonProcessingException {
    AddClothesRequest request = new ObjectMapper().readValue(requestJson, AddClothesRequest.class);
    clothesService.addClothes(userId, request, photo);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(201, "옷 등록 성공", null));
  }
}
