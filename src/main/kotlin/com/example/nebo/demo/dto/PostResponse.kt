package com.example.nebo.demo.dto

import java.time.Instant

data class PostResponse(
    val id: Long,
    val authorId: Long,
    val authorName: String,
    val authorAvatarUrl: String?,
    val drawingId: Long,
    val drawingUrl: String,
    val description: String,
    val createdAt: Instant,
    val likesCount: Long,
    val isLikedByCurrentUser: Boolean = false
)