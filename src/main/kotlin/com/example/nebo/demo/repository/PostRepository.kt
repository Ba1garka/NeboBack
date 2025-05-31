package com.example.nebo.demo.repository

import com.example.nebo.demo.model.Post
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PostRepository : JpaRepository<Post, Long> {
    fun findByAuthorIdOrderByCreatedAtDesc(userId: Long): List<Post>

    fun findAllByOrderByCreatedAtDesc(): List<Post>

    @EntityGraph(attributePaths = ["author", "drawing"])
    fun findAllWithAuthorAndDrawingByOrderByCreatedAtDesc(): List<Post>

    @Query("SELECT COUNT(pl) FROM Post p JOIN p.likedByUsers pl WHERE p.id = :postId")
    fun countLikesByPostId(postId: Long): Long

    @Query("SELECT CASE WHEN COUNT(pl) > 0 THEN true ELSE false END FROM Post p JOIN p.likedByUsers pl WHERE p.id = :postId AND pl.id = :userId")
    fun isPostLikedByUser(postId: Long, userId: Long): Boolean
}