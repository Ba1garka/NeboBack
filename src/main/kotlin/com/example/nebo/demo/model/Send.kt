package com.example.nebo.demo.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "sends")
data class Send(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "drawing_id", nullable = false)
    val drawing: Drawing,

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: User,

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    val recipient: User,

    @Column(name = "sent_at", nullable = false)
    val sentAt: LocalDateTime = LocalDateTime.now()
)