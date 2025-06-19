package com.example.nebo.demo.repository

import com.example.nebo.demo.model.Drawing
import com.example.nebo.demo.model.MyUser
import org.springframework.data.jpa.repository.JpaRepository

interface DrawingRepository : JpaRepository<Drawing, Long> {
    fun findByUser(user: MyUser): List<Drawing>
}