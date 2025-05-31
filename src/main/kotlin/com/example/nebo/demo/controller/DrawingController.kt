package com.example.nebo.demo.controller

import com.example.nebo.demo.dto.DrawingResponse
import com.example.nebo.demo.dto.UserSimpleDto
import com.example.nebo.demo.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import com.example.nebo.demo.service.DrawingService
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder


@RestController
@RequestMapping("/api/drawings")
class DrawingController(
    private val drawingService: DrawingService,
    private val userRepository: UserRepository
) {
    @PostMapping("/upload")
    @Transactional
    fun uploadDrawing(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<DrawingResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!authentication.isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        println("Received file: ${file.originalFilename}, size: ${file.size}")
        println("Authenticated user: ${authentication.name}")

        val user = userRepository.findByEmail(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        return try {
            val drawing = drawingService.uploadDrawing(file, user)
            val response = drawing.id?.let {
                drawing.user.id?.let { it1 ->
                    UserSimpleDto(
                        id = it1,
                        email = drawing.user.email,
                        name = drawing.user.name
                    )
                }?.let { it2 ->
                    DrawingResponse(
                        id = it,
                        title = drawing.title,
                        filePath = drawing.filePath,
                        createdAt = drawing.createdAt,
                        user = it2
                    )
                }
            }
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            println("Error uploading drawing: ${e.message}")
            throw e
        }
    }

    @GetMapping("/my")
    @Transactional
    fun getMyDrawings(): ResponseEntity<List<DrawingResponse>> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!authentication.isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        return try {
            val drawings = drawingService.getDrawingsByUserEmail(authentication.name)
            val response = drawings.map { drawing ->
                DrawingResponse(
                    id = drawing.id!!,
                    title = drawing.title,
                    filePath = drawing.filePath,
                    createdAt = drawing.createdAt,
                    user = UserSimpleDto(
                        id = drawing.user.id!!,
                        email = drawing.user.email,
                        name = drawing.user.name
                    )
                )
            }
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            println("Error fetching drawings: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}