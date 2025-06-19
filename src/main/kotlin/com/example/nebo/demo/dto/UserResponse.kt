package com.example.nebo.demo.dto

import com.example.nebo.demo.model.MyUser

data class UserResponse(
    val email: String,
    val name: String,
    val birthDate: String,
    val roles: Set<String>,
    val drawingsCount: Int,
    val avatarUrl: String?
) {
    companion object {
        fun fromUser(user: MyUser): UserResponse {
            return UserResponse(
                email = user.email,
                name = user.name,
                birthDate = user.birthDate.toString(),
                roles = user.roles,
                drawingsCount = user.drawings.size,
                avatarUrl = user.filePath
            )
        }
    }
}