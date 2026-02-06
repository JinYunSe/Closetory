package com.ssafy.closetory.controller.s3;

import com.ssafy.closetory.dto.common.ApiResponse;
import com.ssafy.closetory.service.s3.S3ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/images")
public class ImageController {

  private final S3ImageService s3ImageService;

  public ImageController(S3ImageService s3ImageService) {
    this.s3ImageService = s3ImageService;
  }

  // 업로드
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "이미지 업로드")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Map<String, String>>> upload(
      @RequestPart("file") MultipartFile file) {
    String url = s3ImageService.upload(file);
    return ResponseEntity.ok(ApiResponse.ok(200, "이미지 업로드 성공", Map.of("url", url)));
  }

  // 삭제
  @DeleteMapping
  @Operation(summary = "이미지 삭제")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> delete(@RequestParam String url) {
    s3ImageService.deleteByUrl(url);
    return ResponseEntity.ok(ApiResponse.ok(200, "이미지 삭제 성공", null));
  }
}
