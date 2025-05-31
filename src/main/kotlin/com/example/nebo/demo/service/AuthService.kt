package com.example.nebo.demo.service

import com.example.nebo.demo.dto.RegisterRequest
import com.example.nebo.demo.model.Drawing
import com.example.nebo.demo.model.User
import com.example.nebo.demo.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val minioService: MinioService
) {
    fun register(request: RegisterRequest): User {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists")
        }

        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            name = request.name,
            birthDate = request.birthDate,
            filePath = null.toString()
        )

        return userRepository.save(user)
    }

    fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found")
    }

    fun getUserDrawings(email: String): List<Drawing> {
        return getUserByEmail(email).drawings
    }

    fun uploadDrawing(file: MultipartFile, user: User): Drawing {
        val objectName = "drawings/${user.id}/${UUID.randomUUID()}_${file.originalFilename}"
        val filePath = minioService.uploadFile(file, objectName)

        val drawing = Drawing(
            title = file.originalFilename ?: "Untitled",
            filePath = filePath,
            user = user
        )

        user.drawings.add(drawing)
        userRepository.save(user)
        return drawing
    }

    fun updateUserAvatar(email: String, file: MultipartFile): String {
        val user = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("User not found")

        // Удаляем старый аватар, если он есть
        user.filePath?.let { oldAvatarPath ->
            try {
                minioService.deleteFile(oldAvatarPath)
            } catch (e: Exception) {
                println("Failed to delete old avatar: ${e.message}")
            }
        }

        // Генерируем уникальное имя файла
        val fileExtension = file.originalFilename?.substringAfterLast('.', "")
        val objectName = "avatars/${UUID.randomUUID()}${if (fileExtension != null) ".$fileExtension" else ""}"

        // Загружаем новый аватар
        val avatarUrl = minioService.uploadFile(file, objectName)

        // Обновляем пользователя
        user.filePath = avatarUrl
        userRepository.save(user)

        return avatarUrl
    }

}