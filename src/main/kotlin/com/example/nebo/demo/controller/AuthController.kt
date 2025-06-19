package com.example.nebo.demo.controller

import com.example.nebo.demo.dto.RegisterRequest
import com.example.nebo.demo.dto.UserResponse
import com.example.nebo.demo.model.MyUser
import com.example.nebo.demo.repository.UserRepository
import com.example.nebo.demo.service.AuthService
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import org.hibernate.Hibernate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val entityManager: EntityManager,
    private val authService: AuthService
) {

    @PostMapping("/avatar")
    @Transactional
    fun uploadAvatar(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, String>> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!authentication.isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        return try {
            val avatarUrl = authService.updateUserAvatar(authentication.name, file)
            ResponseEntity.ok(mapOf("avatarUrl" to avatarUrl))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<MyUser> {
        return ResponseEntity.ok(authService.register(request))
    }

    @GetMapping("/me")
    @Transactional
    fun getCurrentUser(): ResponseEntity<Any> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!authentication.isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val email = authentication.name
        val user = userRepository.findByEmail(email)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        Hibernate.initialize(user.drawings)

        return ResponseEntity.ok(createUserResponse(user))
    }

    private fun createUserResponse(user: MyUser): UserResponse {
        return UserResponse.fromUser(user)
    }


}