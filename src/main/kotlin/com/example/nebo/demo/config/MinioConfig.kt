package com.example.nebo.demo.config

import com.example.nebo.demo.model.MinioProperties
import io.minio.MinioClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(MinioProperties::class)
class MinioConfig(private val minioProperties: MinioProperties) {

    @Bean
    fun minioClient(): MinioClient {
        return try {
            MinioClient.builder()
                .endpoint(minioProperties.endpoint)
                .credentials(minioProperties.accessKey, minioProperties.secretKey)
                .build()
        } catch (e: Exception) {
            throw Exception("Failed to create MinIO client: ${e.message}", e)
        }
    }
}