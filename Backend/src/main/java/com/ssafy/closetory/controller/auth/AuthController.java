package com.ssafy.closetory.controller.auth;

import com.ssafy.closetory.dto.auth.LoginRequest;
import com.ssafy.closetory.dto.auth.LoginResponse;
import com.ssafy.closetory.dto.auth.SignupRequest;
import com.ssafy.closetory.dto.common.ApiResponse;
import com.ssafy.closetory.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signup")
  @Operation(summary = "회원가입")
  public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
    authService.signup(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(201, "회원가입 성공", null));
  }

  @PostMapping("/login")
  @Operation(summary = "로그인")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest request) {

    LoginResponse response = authService.login(request);

    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "로그인 성공", response));
  }

  @PostMapping("/logout")
  @Operation(summary = "로그아웃")
  public ResponseEntity<ApiResponse<Void>> logout(
      @RequestHeader("X-REFRESH-TOKEN") String refreshToken) {
    authService.logout(refreshToken);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "로그아웃 성공", null));
  }

  @PostMapping("/token")
  @Operation(summary = "토큰 재발급")
  public ResponseEntity<ApiResponse<LoginResponse>> token(
      @RequestHeader("X-REFRESH-TOKEN") String refreshToken) {

    LoginResponse response = authService.token(refreshToken);

    return ResponseEntity.ok(ApiResponse.ok(200, "토큰 재발급 성공", response));
  }
}
