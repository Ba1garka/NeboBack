package com.example.nebo.demo.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpRequestResponseHolder
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Component

@Component
class CookieSecurityContextRepository(
    private val objectMapper: ObjectMapper
) : SecurityContextRepository {

    companion object {
        const val COOKIE_NAME = "AUTH_CONTEXT"
    }

    override fun loadContext(holder: HttpRequestResponseHolder): SecurityContext {
        return loadContext(holder.request) ?: SecurityContextHolder.createEmptyContext()
    }

    private fun loadContext(request: HttpServletRequest): SecurityContext? {
        return request.cookies?.find { it.name == COOKIE_NAME }?.value?.let { cookieValue ->
            try {
                objectMapper.readValue(cookieValue, SecurityContext::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun saveContext(
        context: SecurityContext,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (context.authentication != null && context.authentication.isAuthenticated) {
            val cookie = Cookie(COOKIE_NAME, objectMapper.writeValueAsString(context)).apply {
                maxAge = 30 * 60 // 30 минут
                path = "/"
                secure = true
                // Устанавливаем httpOnly через метод
                isHttpOnly = true
                setAttribute("SameSite", "None")
            }
            response.addCookie(cookie)
        } else {
            val cookie = Cookie(COOKIE_NAME, "").apply {
                maxAge = 0
                path = "/"
            }
            response.addCookie(cookie)
        }
    }

    override fun containsContext(request: HttpServletRequest): Boolean {
        return request.cookies?.any { it.name == COOKIE_NAME } ?: false
    }
}