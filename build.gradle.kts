plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.6"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
}

group = "com.example.nebo"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.session:spring-session-core")

	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	//implementation("org.springframework.session:spring-session-jdbc")
	// Для инициализации БД
	//implementation("org.springframework.boot:spring-boot-starter-jdbc")// Для хранения сессий в БД
	implementation("org.flywaydb:flyway-database-postgresql:10.8.1")


	// Kotlin
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// База данных
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("com.h2database:h2")  // Для тестов

	// MinIO
	implementation("io.minio:minio:8.5.7")  // Актуальная версия MinIO SDK
	implementation("org.springframework.boot:spring-boot-configuration-processor")
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	// Dev
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	// Тесты
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
