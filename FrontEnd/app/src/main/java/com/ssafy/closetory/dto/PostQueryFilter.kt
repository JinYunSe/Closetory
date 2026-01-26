package com.ssafy.closetory.dto

// 서버 query parameter filter 값 관리
enum class PostQueryFilter(val value: String) {
    LIKED("liked"), // 내가 좋아요한 게시글
    WRITTEN("written"), // 내가 쓴 게시글
    LATEST("latest"), // 최신 순
    POPULAR("popular"); // 추천 순

    override fun toString(): String = value
}
