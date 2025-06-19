package com.example.nebo.demo.service

import com.example.nebo.demo.dto.RegisterRequest
import com.example.nebo.demo.model.Drawing
import com.example.nebo.demo.model.MyUser
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
    fun register(request: RegisterRequest): MyUser {
        if (userRepository.existsByEmail(request.email)) {
            throw Exception("Email already exists")
        }

        val user = MyUser(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            name = request.name,
            birthDate = request.birthDate,
            filePath = null.toString()
        )

        return userRepository.save(user)
    }

    fun updateUserAvatar(email: String, file: MultipartFile): String {
        val user = userRepository.findByEmail(email) ?: throw Exception("User not found")

        // Удаляем старый аватар
        user.filePath?.let { oldAvatarPath ->
            try {
                minioService.deleteFile(oldAvatarPath)
            } catch (e: Exception) {
                println("Failed to delete old avatar: ${e.message}")
            }
        }

        val fileExtension = file.originalFilename?.substringAfterLast('.', "")
        val objectName = "avatars/${UUID.randomUUID()}${if (fileExtension != null) ".$fileExtension" else ""}"

        val avatarUrl = minioService.uploadFile(file, objectName)

        user.filePath = avatarUrl
        userRepository.save(user)

        return avatarUrl
    }

}