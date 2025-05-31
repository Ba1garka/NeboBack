package com.example.nebo.demo.config

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

object SecurityUtils {

    fun getCurrentUsername(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.takeIf { it.isAuthenticated }?.name
    }

    fun getCurrentUserDetails(): UserDetails? {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.takeIf { it.isAuthenticated }?.principal as? UserDetails
    }

    fun getCurrentUserEmail(): String? {
        return getCurrentUsername()
    }
}