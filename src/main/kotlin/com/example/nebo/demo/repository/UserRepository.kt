package com.example.nebo.demo.repository

import com.example.nebo.demo.model.MyUser
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<MyUser, Long> {
    fun findByEmail(email: String): MyUser?
    fun existsByEmail(email: String): Boolean
    fun findByName(name: String): MyUser?

}