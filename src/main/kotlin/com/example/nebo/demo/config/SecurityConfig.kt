package com.example.nebo.demo.config

import com.example.nebo.demo.service.UserDetailsServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.invoke
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.security.web.context.DelegatingSecurityContextRepository
import org.springframework.security.web.context.HttpRequestResponseHolder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.session.InvalidSessionStrategy
import org.springframework.security.web.session.SessionManagementFilter
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException


//@Configuration
//@EnableWebSecurity
//class SecurityConfig (
//    private val cookieSecurityContextRepository: CookieSecurityContextRepository
//) {
//
//    @Bean
//    fun securityFilterChain(
//        http: HttpSecurity,
//        authManager: AuthenticationManager
//    ): SecurityFilterChain {
//        val jsonAuthFilter = JsonUsernamePasswordAuthenticationFilter(authManager).apply {
//            setAuthenticationSuccessHandler { _, response, _ ->
//                response.status = HttpStatus.OK.value()
//                response.contentType = MediaType.APPLICATION_JSON_VALUE
//                response.writer.write("""{"message":"Login successful"}""")
//            }
//            setAuthenticationFailureHandler { _, response, exception ->
//                response.status = HttpStatus.UNAUTHORIZED.value()
//                response.contentType = MediaType.APPLICATION_JSON_VALUE
//                response.writer.write("""{"error":"${exception.message}"}""")
//            }
//        }
//
//        http {
//            csrf { disable() }
//            cors { configurationSource = corsConfigurationSource() }
//            sessionManagement {
//                sessionCreationPolicy = SessionCreationPolicy.ALWAYS
//                sessionFixation {
//                    migrateSession()
//                }
//                invalidSessionStrategy = CustomInvalidSessionStrategy()
//            }
//            authorizeRequests {
//                authorize("/api/auth/register", permitAll)
//                authorize(anyRequest, authenticated)
//            }
//            exceptionHandling {
//                authenticationEntryPoint = HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
//            }
//            securityContext {
//                securityContextRepository = securityContextRepository()
//            }
//            addFilterBefore(jsonAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
//        }
//
//        return http.build()
//    }
//
//    @Bean
//    fun securityContextRepository(): SecurityContextRepository {
//        return DelegatingSecurityContextRepository(
//            cookieSecurityContextRepository,
//            HttpSessionSecurityContextRepository()
//        )
//    }
//
//    @Bean
//    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
//        return authConfig.authenticationManager
//    }
//
//    @Bean
//    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
//
//    private fun corsConfigurationSource(): CorsConfigurationSource {
//        val configuration = CorsConfiguration().apply {
//            allowedOrigins = listOf("*")
//            allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
//            allowedHeaders = listOf("*")
//            allowCredentials = true
//            exposedHeaders = listOf("Set-Cookie", "Authorization")
//            maxAge = 3600
//        }
//        return UrlBasedCorsConfigurationSource().apply {
//            registerCorsConfiguration("/**", configuration)
//        }
//    }
//
//    class CustomInvalidSessionStrategy : InvalidSessionStrategy {
//        override fun onInvalidSessionDetected(request: HttpServletRequest, response: HttpServletResponse) {
//            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Session expired")
//        }
//    }
//}

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val userDetailsService: UserDetailsServiceImpl,
    private val cookieSecurityContextRepository: CookieSecurityContextRepository
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            cors { configurationSource = corsConfigurationSource() }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.ALWAYS
                sessionFixation { migrateSession() }
                invalidSessionStrategy = CustomInvalidSessionStrategy()
            }
            securityContext {
                securityContextRepository = securityContextRepository()
            }
            authorizeHttpRequests {
                authorize("/api/auth/**", permitAll)
                authorize(anyRequest, authenticated)
            }
            exceptionHandling {
                authenticationEntryPoint = HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
            }
            formLogin { disable() }
            httpBasic { disable() }
            addFilterBefore(jsonAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
            addFilterBefore(SecurityContextPersistenceFilter(), SessionManagementFilter::class.java)
            logout {
                logoutUrl = "/api/auth/logout"
                deleteCookies("JSESSIONID", "AUTH_SESSION")
                invalidateHttpSession = true
                logoutSuccessHandler = HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)
            }
        }
        return http.build()
    }

    // Добавляем класс CustomInvalidSessionStrategy
    class CustomInvalidSessionStrategy : InvalidSessionStrategy {
        override fun onInvalidSessionDetected(request: HttpServletRequest, response: HttpServletResponse) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Session expired")
        }
    }

    @Bean
    fun jsonAuthenticationFilter(): JsonUsernamePasswordAuthenticationFilter {
        return JsonUsernamePasswordAuthenticationFilter(authenticationManager()).apply {
            setAuthenticationSuccessHandler { request, response, authResult ->
                // Явное сохранение контекста
                val context = SecurityContextHolder.getContext()
                context.authentication = authResult
                request.session.setAttribute("SPRING_SECURITY_CONTEXT", context)

                response.status = HttpStatus.OK.value()
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                response.writer.write("""{"message":"Login successful"}""")
            }
            setAuthenticationFailureHandler { _, response, exception ->
                response.status = HttpStatus.UNAUTHORIZED.value()
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                response.writer.write("""{"error":"${exception.message}"}""")
            }
        }
    }

    @Bean
    fun authenticationManager(): AuthenticationManager {
        val authProvider = DaoAuthenticationProvider().apply {
            setUserDetailsService(userDetailsService)
            setPasswordEncoder(passwordEncoder())
        }
        return ProviderManager(authProvider)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityContextRepository(): SecurityContextRepository {
        return DelegatingSecurityContextRepository(
            cookieSecurityContextRepository,
            HttpSessionSecurityContextRepository()
        )
    }

    private fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
            exposedHeaders = listOf("Set-Cookie")
            maxAge = 3600
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }

    @Component
    class SecurityContextPersistenceFilter : OncePerRequestFilter() {
        override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
        ) {
            request.getSession(false)?.getAttribute("SPRING_SECURITY_CONTEXT")?.let {
                SecurityContextHolder.setContext(it as SecurityContext)
            }
            try {
                filterChain.doFilter(request, response)
            } finally {
                request.getSession(false)?.setAttribute(
                    "SPRING_SECURITY_CONTEXT",
                    SecurityContextHolder.getContext()
                )
            }
        }
    }
}
