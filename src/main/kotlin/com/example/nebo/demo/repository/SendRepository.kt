package com.example.nebo.demo.repository

import com.example.nebo.demo.model.Send
import org.springframework.data.jpa.repository.JpaRepository

interface SendRepository : JpaRepository<Send, Long> {
    fun findAllByRecipientId(recipientId: Long): List<Send>
}