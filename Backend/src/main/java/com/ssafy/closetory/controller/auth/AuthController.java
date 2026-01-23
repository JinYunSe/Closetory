package com.ssafy.closetory.controller.auth;

import com.ssafy.closetory.dto.auth.LoginRequest;
import com.ssafy.closetory.dto.auth.LoginResponse;
import com.ssafy.closetory.dto.auth.SignupRequest;
import com.ssafy.closetory.dto.common.ApiResponse;
import com.ssafy.closetory.exception.common.UnauthorizedException;
import com.ssafy.closetory.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(200, "로그인 성공", response));
  }

  @PostMapping("/logout")
  @Operation(summary = "로그아웃")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Integer userId) {
    authService.logout(userId);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "로그아웃 성공", null));
  }

  //  토큰 재발급
  @PostMapping("/token")
  @Operation(summary = "토큰 재발급")
  public ResponseEntity<ApiResponse<LoginResponse>> token(
      @RequestHeader("Authorization") String authorization,
      @RequestHeader("X-USER-ID") Integer userId) {

    // Refresh Token 추출
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      throw new UnauthorizedException("리프레시 토큰이 없습니다.");
    }

    String refreshToken = authorization.substring(7);

    LoginResponse response = authService.token(userId, refreshToken);

    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "토큰 재발급 성공", response));
  }
}
