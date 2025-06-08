package com.example.nebo.demo.service

import com.example.nebo.demo.dto.SendDto
import com.example.nebo.demo.model.Drawing
import com.example.nebo.demo.model.Send
import com.example.nebo.demo.model.User
import com.example.nebo.demo.repository.SendRepository
import org.springframework.stereotype.Service

@Service
class SendService(
    private val sendRepository: SendRepository
) {
    fun createSend(drawing: Drawing, sender: User, recipient: User): Send {
        val send = Send(
            drawing = drawing,
            sender = sender,
            recipient = recipient
        )
        return sendRepository.save(send)
    }

    fun getReceivedSends(recipientId: Long): List<SendDto> {
        return sendRepository.findAllByRecipientId(recipientId).map { it.toDto() }
    }

    fun Send.toDto() = SendDto(
        id = id!!,
        drawingId = drawing.id!!,
        drawingTitle = drawing.title,
        drawingPath = drawing.filePath,
        senderId = sender.id!!,
        senderName = sender.name,
        senderAvatarPath = sender.filePath,
        recipientId = recipient.id!!,
        recipientName = recipient.name,
        sentAt = sentAt
    )
}