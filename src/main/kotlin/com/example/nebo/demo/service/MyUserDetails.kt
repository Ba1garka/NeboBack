package com.example.nebo.demo.service

import com.example.nebo.demo.model.MyUser
import com.example.nebo.demo.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
@Transactional
class MyUserDetails(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            ?: throw Exception("User not found with email: $email")

        return User(
            user.email,
            user.password,
            user.roles.map { SimpleGrantedAuthority("ROLE_$it") }
        )
    }
}