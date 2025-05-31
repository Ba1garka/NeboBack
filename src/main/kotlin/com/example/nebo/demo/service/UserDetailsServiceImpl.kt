package com.example.nebo.demo.service

import com.example.nebo.demo.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
@Transactional
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found with email: $email")

        return org.springframework.security.core.userdetails.User(
            user.email,
            user.password,
            user.roles.map { SimpleGrantedAuthority("ROLE_$it") }
        )
    }
}