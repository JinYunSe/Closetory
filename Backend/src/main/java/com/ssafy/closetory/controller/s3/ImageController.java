package com.ssafy.closetory.controller.s3;

import com.ssafy.closetory.service.s3.S3ImageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
public class ImageController {

  private final S3ImageService s3ImageService;

  public ImageController(S3ImageService s3ImageService) {
    this.s3ImageService = s3ImageService;
  }

  // 업로드
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @SecurityRequirement(name = "bearerAuth")
  public Map<String, String> upload(@RequestPart("file") MultipartFile file) {
    String url = s3ImageService.upload(file);
    return Map.of("url", url);
  }

  // 삭제
  @DeleteMapping
  @SecurityRequirement(name = "bearerAuth")
  public Map<String, String> delete(@RequestParam String url) {
    s3ImageService.deleteByUrl(url);
    return Map.of("result", "deleted");
  }
}
