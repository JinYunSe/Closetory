package com.ssafy.closetory.controller.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.closetory.dto.common.ApiResponse;
import com.ssafy.closetory.dto.user.*;
import com.ssafy.closetory.service.user.StatsService;
import com.ssafy.closetory.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

  private final UserService userService;
  private final StatsService statsService;
  private final ObjectMapper objectMapper;

  @PatchMapping(
      value = "/{userId}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "회원 정보 수정")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> updateUser(
      @PathVariable Integer userId,
      @AuthenticationPrincipal Integer authUserId,
      @RequestPart(value = "profilePhoto", required = false) MultipartFile profilePhoto,
      @RequestPart(value = "bodyPhoto", required = false) MultipartFile bodyPhoto,
      @Valid @RequestPart(value = "request", required = false) String requestJson)
      throws JsonProcessingException {
    UpdateUserRequest request = null;
    if (requestJson != null) {
      request = objectMapper.readValue(requestJson, UpdateUserRequest.class);
    }

    userService.updateUser(authUserId, userId, request, profilePhoto, bodyPhoto);
    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.ok(200, "회원정보가 수정 완료 되었습니다.", null));
  }

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

  @GetMapping("/{userId}")
  @Operation(summary = "회원 정보 조회")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<UserDetailResponse>> getUser(
      @PathVariable Integer userId, @AuthenticationPrincipal Integer authUserId) {
    UserDetailResponse response = userService.getUserDetail(authUserId, userId);

    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "회원정보 조회 성공", response));
  }

  @DeleteMapping("/{userId}")
  @Operation(summary = "회원 탈퇴")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> deleteUser(
      @PathVariable Integer userId,
      @AuthenticationPrincipal Integer authUserId,
      @Valid @RequestBody UserDeleteRequest request) {
    userService.deleteUser(authUserId, userId, request.password());
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "회원탈퇴가 완료 되었습니다.", null));
  }

  @GetMapping("/{userId}/stats/top3")
  @Operation(summary = "이번 달에 가장 자주 입은 옷 Top3")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<List<Top3Item>>> getTop3(
      @PathVariable Integer userId, @AuthenticationPrincipal Integer authUserId) {
    List<Top3Item> response = statsService.getTop3(userId, authUserId);
    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.ok(200, "이번 달에 가장 자주 입은 옷 Top3 조회 성공", response));
  }
}
