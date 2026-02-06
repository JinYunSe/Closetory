package com.ssafy.closetory.exception.s3;

public class S3UploadException extends RuntimeException {
  public S3UploadException(String message, Throwable cause) {
    super(message, cause);
  }
}
