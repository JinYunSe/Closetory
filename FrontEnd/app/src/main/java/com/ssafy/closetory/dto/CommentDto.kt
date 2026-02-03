package com.ssafy.closetory.dto

// data class CommentDto(
//    val commentId: Int,
//    val nickname: String,
//    val content: String,
//    val profileImage: String?,
//    val createdAt: String,
//    val isMine: Boolean
// )

import com.google.gson.annotations.SerializedName

data class CommentDto(
    @SerializedName("commentId")
    val commentId: Int,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("profileImage")
    val profileImage: String? = null,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("isMine")
    val isMine: Boolean
)
