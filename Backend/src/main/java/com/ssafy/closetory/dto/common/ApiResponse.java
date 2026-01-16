package com.ssafy.closetory.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  private final int httpStatusCode;
  private final String responseMessage;
  private final T data;
  private final String errorMessage;

  private ApiResponse(int httpStatusCode, String responseMessage, T data, String errorMessage) {
    this.httpStatusCode = httpStatusCode;
    this.responseMessage = responseMessage;
    this.data = data;
    this.errorMessage = errorMessage;
  }

  // 성공
  public static <T> ApiResponse<T> ok(int statusCode, String message, T data) {
    return new ApiResponse<>(statusCode, message, data, null);
  }

  // 실패
  public static ApiResponse<Void> fail(int statusCode, String errorMessage) {
    return new ApiResponse<>(statusCode, null, null, errorMessage);
  }
}
