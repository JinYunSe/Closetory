package com.ssafy.closetory.exception.common;

import com.ssafy.closetory.dto.common.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 400 Bad Request - 요청 값/형식 오류
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
  }


  // 서비스에서 직접 던진 중복 예외
  @ExceptionHandler(DuplicateKeyException.class)
  public ResponseEntity<ApiResponse<Void>> handleDuplicateKey(DuplicateKeyException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.fail(409, e.getMessage()));
  }

  //  DB UNIQUE 제약 위반
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
      DataIntegrityViolationException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiResponse.fail(409, "이미 사용중인 아이디 또는 닉네임입니다."));
  }


  // 401 Unauthorized - 인증 실패
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
  }

  // 403 Forbidden - 권한 없음
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiResponse<Void>> handleForbidden(ForbiddenException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ApiResponse.fail(HttpStatus.FORBIDDEN.value(), e.getMessage()));
  }

  // 404 Not Found - 대상 리소스 없음
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), e.getMessage()));
  }

  // 409 Conflict - 상태 충돌
  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiResponse.fail(HttpStatus.CONFLICT.value(), e.getMessage()));
  }

  // 500 Internal Server Error - 서버 오류

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 오류가 발생했습니다."));
  }
}
