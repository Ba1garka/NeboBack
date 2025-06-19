package com.example.nebo.demo.service

import io.minio.*
import io.minio.http.Method
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeUnit
import com.example.nebo.demo.model.MinioProperties

@Service
class MinioService @Autowired constructor(private val minioClient: MinioClient, private val minioProperties: MinioProperties) {

    init {
        createBucketIfNotExists()
    }

    private fun createBucketIfNotExists() {
        try {
            if (!minioClient.bucketExists(
                    BucketExistsArgs.builder()
                        .bucket(minioProperties.bucketName)
                        .build()
                )) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(minioProperties.bucketName)
                        .build()
                )
            }
        } catch (e: Exception) {
            throw Exception("MinIO bucket operation failed", e)
        }
    }

    fun uploadFile(file: MultipartFile, objectName: String): String {
        require(file.size > 0) { "File cannot be empty" }

        return try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioProperties.bucketName)
                    .`object`(objectName)
                    .stream(file.inputStream, file.size, -1)
                    .contentType(file.contentType)
                    .build()
            )
            getFileUrl(objectName)
        } catch (e: Exception) {
            throw Exception("Failed to upload file to MinIO", e)
        }
    }

    private fun getFileUrl(objectName: String): String {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(minioProperties.bucketName)
                .`object`(objectName)
                .expiry(7, TimeUnit.DAYS)
                .build()
        )
    }

    fun deleteFile(objectPath: String) {
        try {
            val cleanPath = objectPath.removePrefix("http://localhost:9000/nebo/")
            val objectName = cleanPath.substringBefore('?')
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(minioProperties.bucketName)
                    .`object`(objectName)
                    .build()
            )
        } catch (e: Exception) {
            throw Exception("Failed to delete file from MinIO", e)
        }
    }

}