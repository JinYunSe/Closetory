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

  // 잘못된 요청
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.fail(400, e.getMessage()));
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

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.fail(500, "서버 오류가 발생했습니다."));
  }
}
