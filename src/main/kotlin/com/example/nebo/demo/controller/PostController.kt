package com.example.nebo.demo.controller

import com.example.nebo.demo.dto.CreatePostRequest
import com.example.nebo.demo.dto.PostResponse
import com.example.nebo.demo.repository.UserRepository
import com.example.nebo.demo.service.PostService
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/drawings")
class PostController(
    private val postService: PostService,
    private val userRepository: UserRepository
) {
    @PostMapping("/post")
    @Transactional
    fun createPost(
        @RequestBody request: CreatePostRequest
    ): ResponseEntity<PostResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!authentication.isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        println("Creating post for user: ${authentication.name}")
        println("Post details: drawingId=${request.drawingId}, description=${request.description}")

        val user = userRepository.findByEmail(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        return try {
            val post = postService.createPost(user, request)
            println("Successfully created post: $post")

            val response = post.id?.let { postId ->
                PostResponse(
                    id = postId,
                    authorId = user.id!!,
                    authorName = user.name,
                    authorAvatarUrl = user.filePath,
                    drawingId = post.drawing.id!!,
                    drawingUrl = post.drawing.filePath,
                    description = post.description,
                    createdAt = post.createdAt,
                    likesCount = post.likesCount
                )
            }

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            println("Error creating post: ${e.message}")
            throw e
        }
    }

    @GetMapping("/all")
    fun getAllPosts(): ResponseEntity<List<PostResponse>> {
        return try {
            val posts = postService.getAllPosts()
            ResponseEntity.ok(posts)
        } catch (e: Exception) {
            println("Error fetching posts: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/like/{postId}")
    @Transactional
    fun likePost(
        @PathVariable postId: Long
    ): ResponseEntity<PostResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!authentication.isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val user = userRepository.findByEmail(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        println("id: " + user.id.toString())

        return try {
            val updatedPost = postService.likePost(postId, user)
            println("Successfully updated likes for post: $postId. New like count: ${updatedPost.likesCount}")

            val response = PostResponse(
                id = updatedPost.id!!,
                authorId = updatedPost.author.id!!,
                authorName = updatedPost.author.name,
                authorAvatarUrl = updatedPost.author.filePath,
                drawingId = updatedPost.drawing.id!!,
                drawingUrl = updatedPost.drawing.filePath,
                description = updatedPost.description,
                createdAt = updatedPost.createdAt,
                likesCount = updatedPost.likesCount,
                isLikedByCurrentUser = true
            )

            ResponseEntity.ok(response)
        } catch (e: EntityNotFoundException) {
            println("Post not found: $postId")
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            println("Error processing like: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/unlike/{postId}")
    @Transactional
    fun unlikePost(
        @PathVariable postId: Long
    ): ResponseEntity<PostResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!authentication.isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val user = userRepository.findByEmail(authentication.name)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        return try {
            val updatedPost = postService.unlikePost(postId, user)
            println("Successfully updated likes for post: $postId. New like count: ${updatedPost.likesCount}")

            val response = PostResponse(
                id = updatedPost.id!!,
                authorId = updatedPost.author.id!!,
                authorName = updatedPost.author.name,
                authorAvatarUrl = updatedPost.author.filePath,
                drawingId = updatedPost.drawing.id!!,
                drawingUrl = updatedPost.drawing.filePath,
                description = updatedPost.description,
                createdAt = updatedPost.createdAt,
                likesCount = updatedPost.likesCount,
                isLikedByCurrentUser = true
            )

            ResponseEntity.ok(response)
        } catch (e: EntityNotFoundException) {
            println("Post not found: $postId")
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            println("Error processing like: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}