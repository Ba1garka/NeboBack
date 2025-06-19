package com.example.nebo.demo.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "drawings")
data class Drawing(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(length = 500)
    val title: String,
    @Column(name = "file_path", nullable = false, length = 2000)
    val filePath: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: MyUser,
    //связь с постами
    @OneToMany(mappedBy = "drawing", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val posts: MutableList<Post> = mutableListOf(),
    ){
    override fun toString(): String {
        return "Drawing(id=$id, title='$title', userId=${user.id})" //используем только ID пользователя
    }
}
