package com.example.nebo.demo.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextImpl

class JsonUsernamePasswordAuthenticationFilter(
    authManager: AuthenticationManager
) : UsernamePasswordAuthenticationFilter() {

    private val objectMapper = jacksonObjectMapper()

    init {
        setFilterProcessesUrl("/api/auth/login")
        this.authenticationManager = authManager

        setAuthenticationSuccessHandler { request, response, authResult ->
            // Явное сохранение в сессию
            val session = request.getSession(true)
            session.setAttribute(
                "SPRING_SECURITY_CONTEXT",
                SecurityContextImpl(authResult)
            )

            response.status = HttpStatus.OK.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write("""{"message":"Login successful"}""")
        }

        setAuthenticationFailureHandler { _, response, exception ->
            response.status = HttpStatus.UNAUTHORIZED.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write("""{"error":"${exception.message}"}""")
        }
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
            val authToken = UsernamePasswordAuthenticationToken(
                credentials.email,
                credentials.password
            )
            authenticationManager.authenticate(authToken)
        } catch (e: Exception) {
            throw AuthenticationServiceException("Authentication failed", e)
        }
    }
}

data class LoginRequest(
    val email: String,
    val password: String
)