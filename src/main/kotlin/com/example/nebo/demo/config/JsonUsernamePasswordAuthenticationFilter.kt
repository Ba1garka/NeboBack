package com.example.nebo.demo.config

import com.example.nebo.demo.dto.LoginRequest
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class JsonUsernamePasswordAuthenticationFilter( authManager: AuthenticationManager) : UsernamePasswordAuthenticationFilter() {

    private val objectMapper = jacksonObjectMapper()

    init {
        setFilterProcessesUrl("/auth/login")
        this.authenticationManager = authManager
    }

    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Authentication {
        return try {
            val credentials = objectMapper.readValue(
                request.inputStream,
                LoginRequest::class.java
            )
            val authToken = UsernamePasswordAuthenticationToken( // объект-контейнер для учётных данных до проверки
                credentials.email,
                credentials.password
            )
            authenticationManager.authenticate(authToken) //сырой токен, после проверки заменяется на аутентифицированный токен
            // -. DaoAuthenticationProvider -> UserDetailsService
        } catch (e: Exception) {
            throw Exception("Authentication failed", e)
        }
    }
}