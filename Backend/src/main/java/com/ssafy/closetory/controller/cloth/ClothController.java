package com.ssafy.closetory.controller.cloth;

import com.ssafy.closetory.dto.cloth.GetClosetRequest;
import com.ssafy.closetory.dto.cloth.GetClosetResponse;
import com.ssafy.closetory.dto.common.ApiResponse;
import com.ssafy.closetory.service.cloth.ClothService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clothes")
public class ClothController {

  private final ClothService clothService;

  @GetMapping
  @Operation(summary = "옷장 조회")
  public ResponseEntity<ApiResponse<GetClosetResponse>> getCloset(
      @RequestParam(required = false) List<String> tags,
      @RequestParam(required = false) String color,
      @RequestParam(required = false) List<String> seasons,
      @RequestParam(defaultValue = "false") Boolean onlyLike,
      @RequestParam(defaultValue = "true") Boolean onlyMine) {
    Long userId = 1L; // 임시
    GetClosetRequest request = new GetClosetRequest(tags, color, seasons, onlyLike, onlyMine);
    GetClosetResponse response = clothService.getCloset(userId, request);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "옷장 조회 성공", response));
  }
}
