package com.example.nebo.demo.service

import com.example.nebo.demo.model.Drawing
import com.example.nebo.demo.model.MyUser
import com.example.nebo.demo.repository.DrawingRepository
import com.example.nebo.demo.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class DrawingService(
    private val drawingRepository: DrawingRepository,
    private val userRepository: UserRepository,
    private val minioService: MinioService
) {
    fun uploadDrawing(file: MultipartFile, user: MyUser): Drawing {
        val allowedTypes = setOf("image/jpeg", "image/png")
        if (!allowedTypes.contains(file.contentType)) {
            throw IllegalArgumentException("Unsupported file type")
        }

        val objectName = "drawings/${user.id}/${UUID.randomUUID()}_${file.originalFilename}"
        val filePath = minioService.uploadFile(file, objectName)

        return drawingRepository.save(
            Drawing(
                title = file.originalFilename ?: "Untitled",
                filePath = filePath,
                user = user,
                createdAt = LocalDateTime.now()
            )
        )
    }

    @Transactional
    fun getDrawingsByUserEmail(email: String): List<Drawing> {
        val user = userRepository.findByEmail(email) ?: throw Exception("User not found")
        return drawingRepository.findByUser(user)
    }

    @Transactional
    fun getDrawingById(id: Long): Drawing {
        return drawingRepository.findById(id).orElseThrow { Exception("Drawing with id $id not found") }
    }

    @Transactional
    fun deleteDrawing(id: Long) {
        val drawing = getDrawingById(id)
        try {
            println("путь: " + drawing.filePath)
            minioService.deleteFile(drawing.filePath)
            drawingRepository.deleteById(id)
        } catch (e: Exception) {
            throw Exception("Failed to delete drawing with id $id", e)
        }
    }
}