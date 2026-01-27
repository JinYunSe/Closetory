package com.ssafy.closetory.controller.user;

import com.ssafy.closetory.dto.common.ApiResponse;
import com.ssafy.closetory.dto.user.AddStyleRequest;
import com.ssafy.closetory.dto.user.PasswordChangeRequest;
import com.ssafy.closetory.dto.user.PasswordVerifyRequest;
import com.ssafy.closetory.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  // 비밀번호 검증
  @PostMapping("/{userId}/password")
  @Operation(summary = "비밀번호 검증")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> verifyPassword(
      @PathVariable Integer userId, @Valid @RequestBody PasswordVerifyRequest request) {
    userService.verifyPassword(userId, request.password());

    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "비밀번호 확인 완료", null));
  }

  @PatchMapping("/{userId}/password")
  @Operation(summary = "비밀번호 변경")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      @PathVariable Integer userId, @Valid @RequestBody PasswordChangeRequest request) {
    userService.changePassword(userId, request.newPassword(), request.newPasswordConfirm());

    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "비밀번호 변경 완료", null));
  }

  @PostMapping("/{userId}/myStyles")
  @Operation(summary = "사용자 선호 태그 등록")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> addStyle(
      @PathVariable Integer userId,
      @RequestBody AddStyleRequest request,
      @AuthenticationPrincipal Integer authUserId) {
    userService.addStyle(userId, authUserId, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(201, "사용자 선호 태그 등록 완료", null));
  }
}
