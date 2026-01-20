package com.ssafy.closetory.dto

data class ApiResponse<T>(
    val httpStatusCode: Int,
    val responseMessage: String?,
    val errorMessage: String?,
    val data: T?
)
