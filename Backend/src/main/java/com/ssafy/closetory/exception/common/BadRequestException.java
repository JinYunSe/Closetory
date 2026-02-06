package com.ssafy.closetory.exception.common;

public class BadRequestException extends RuntimeException {
  public BadRequestException() {
    super("요청 값이 올바르지 않습니다.");
  }

  public BadRequestException(String message) {
    super(message);
  }
}
