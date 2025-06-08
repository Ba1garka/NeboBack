package com.example.nebo.demo.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "posts")
data class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val author: User,
    @Column(length = 2000)
    val description: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drawing_id", nullable = false)
    val drawing: Drawing,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "likes_count", nullable = false)
    var likesCount: Long = 0,
    @ManyToMany
    @JoinTable(
        name = "post_likes",
        joinColumns = [JoinColumn(name = "post_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    @JsonIgnore
    val likedByUsers: MutableSet<User> = mutableSetOf()
)