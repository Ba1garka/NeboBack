package com.example.nebo.demo.dto

import java.time.LocalDateTime

data class SendDto(
    val id: Long,
    val drawingId: Long,
    val drawingTitle: String,
    val drawingPath: String,
    val senderId: Long,
    val senderName: String,
    val senderAvatarPath: String?,
    val recipientId: Long,
    val recipientName: String,
    val sentAt: LocalDateTime
)