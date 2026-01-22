package com.ssafy.closetory.dto

// 서버가 로그인 성공 시 주는 실제 데이터 부분만 표현
// {
//    "httpStatusCode": 200,
//    "responseMessage": "login success",
//    "data": {
//      "accessToken": "...",
//      "refreshToken": "..."
//    }
// }

data class LoginResponse(val accessToken: String, val refreshToken: String)
