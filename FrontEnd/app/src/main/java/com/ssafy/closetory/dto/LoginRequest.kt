package com.ssafy.closetory.dto

// 요청 형태
// {
//  "userId": "ssafy",
//  "password": "1234"
// }
data class LoginRequest(val userId: String, val password: String)
