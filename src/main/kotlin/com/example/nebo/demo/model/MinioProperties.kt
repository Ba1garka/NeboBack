package com.example.nebo.demo.model

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "minio")
data class MinioProperties(
    val endpoint: String,
    val accessKey: String,
    val secretKey: String,
    val bucketName: String,
    val secure: Boolean = false
)