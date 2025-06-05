package com.example.nebo.demo.service

import com.example.nebo.demo.dto.CreatePostRequest
import com.example.nebo.demo.dto.PostResponse
import com.example.nebo.demo.model.Post
import com.example.nebo.demo.model.User
import com.example.nebo.demo.repository.DrawingRepository
import com.example.nebo.demo.repository.PostRepository
import com.example.nebo.demo.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class PostService(
    private val postRepository: PostRepository,
    private val drawingRepository: DrawingRepository
) {
    fun createPost(author: User, request: CreatePostRequest): Post {
        val drawing = drawingRepository.findById(request.drawingId)
            .orElseThrow { EntityNotFoundException("Drawing not found with id: ${request.drawingId}") }

        if (drawing.user.id != author.id) {
            throw IllegalStateException("Drawing does not belong to the user")
        }

        return postRepository.save(
            Post(
                author = author,
                description = request.description,
                drawing = drawing
            )
        )
    }

    fun getAllPosts(): List<PostResponse> {
        return postRepository.findAllByOrderByCreatedAtDesc().map { post ->
            PostResponse(
                id = post.id!!,
                authorId = post.author.id!!,
                authorName = post.author.name,
                authorAvatarUrl = post.author.filePath,
                drawingId = post.drawing.id!!,
                drawingUrl = post.drawing.filePath,
                description = post.description,
                createdAt = post.createdAt,
                likesCount = post.likesCount
            )
        }
    }

    fun likePost(postId: Long, user: User): Post {
        val post = postRepository.findById(postId)
            .orElseThrow { EntityNotFoundException("Post not found") }

        if (post.likedByUsers.add(user)) {
            post.likesCount++
            return postRepository.save(post)
        }

        return post
    }

    fun unlikePost(postId: Long, user: User): Post {
        val post = postRepository.findById(postId)
            .orElseThrow { EntityNotFoundException("Post not found") }

        if (post.likedByUsers.remove(user)) {
            post.likesCount--
            return postRepository.save(post)
        }

        return post
    }
}