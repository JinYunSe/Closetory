package com.ssafy.closetory.exception.common;

public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException() {
    super("인증이 필요합니다.");
  }

  public UnauthorizedException(String message) {
    super(message);
  }
}
