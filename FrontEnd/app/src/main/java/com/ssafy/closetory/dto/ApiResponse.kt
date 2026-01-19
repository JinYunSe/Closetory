package com.ssafy.closetory.dto

data class ApiResponse<T>(val httpStatusCode: Int, val responseMessage: String, val data: T?)
