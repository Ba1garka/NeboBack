package com.example.nebo.demo.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class RegisterRequest(
    @field:Email
    @field:NotBlank
    val email: String,

    @field:NotBlank
    @field:Size(min = 6)
    val password: String,

    @field:NotBlank
    val name: String,

    @field:Past
    val birthDate: LocalDate
)