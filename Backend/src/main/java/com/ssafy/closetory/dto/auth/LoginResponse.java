package com.ssafy.closetory.dto.auth;

public record LoginResponse(String accessToken, String refreshToken, Integer userId) {}
