package com.example.nebo.demo.controller

import com.example.nebo.demo.dto.SendDto
import com.example.nebo.demo.model.Send
import com.example.nebo.demo.repository.DrawingRepository
import com.example.nebo.demo.repository.UserRepository
import com.example.nebo.demo.service.SendService
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/sends")
class SendController(
    private val sendService: SendService,
    private val userRepository: UserRepository,
    private val drawingRepository: DrawingRepository
) {
    @PostMapping("/send")
    @Transactional
    fun sendDrawing(
        @RequestParam("drawingId") drawingId: Long,
        @RequestParam("recipientName") recipientName: String
    ): ResponseEntity<SendDto> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!authentication.isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        println("Sender: ${authentication.name}")

        val sender = userRepository.findByEmail(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        return try {

            val drawing = drawingRepository.findById(drawingId).orElseThrow { Exception("Drawing not found") }

            if (drawing.user.id != sender.id) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }

            val recipient = userRepository.findByName(recipientName) ?: throw Exception("Recipient with name '$recipientName' not found ")

            val send = sendService.createSend(drawing, sender, recipient)

            ResponseEntity.ok(send.toDto())
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Error sending drawing", e)
        }
    }

    @GetMapping("/my")
    fun getReceivedSends(): ResponseEntity<List<SendDto>> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!authentication.isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val recipient = userRepository.findByEmail(authentication.name) ?: return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        val sends = sendService.getReceivedSends(recipient.id!!)
        return ResponseEntity.ok(sends)
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