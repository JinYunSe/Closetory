package com.ssafy.closetory.controller.clothes;

import com.ssafy.closetory.dto.common.ApiResponse;
import com.ssafy.closetory.entity.clothes.TagItem;
import com.ssafy.closetory.service.clothes.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
public class TagController {

  private final TagService tagService;

  @GetMapping()
  @Operation(summary = "태그 목록 조회")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<List<TagItem>>> getTags() {
    List<TagItem> response = tagService.getTags();
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "태그 목록 조회 성공", response));
  }
}
