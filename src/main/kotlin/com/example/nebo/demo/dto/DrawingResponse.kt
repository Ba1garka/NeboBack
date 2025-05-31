package com.example.nebo.demo.dto

import com.example.nebo.demo.dto.UserSimpleDto
import java.time.LocalDateTime

data class DrawingResponse(
    val id: Long,
    val title: String,
    val filePath: String,
    val createdAt: LocalDateTime,
    val user: UserSimpleDto
)