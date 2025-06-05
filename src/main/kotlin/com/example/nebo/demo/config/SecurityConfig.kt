package com.example.nebo.demo.config

import com.example.nebo.demo.service.MyUserDetails
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.invoke
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.security.web.session.InvalidSessionStrategy
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val myDetails: MyUserDetails
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
            authorizeHttpRequests {
                authorize("/auth/**", permitAll)
                authorize(anyRequest, authenticated)
            }
            exceptionHandling {
                authenticationEntryPoint = HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
            }
            formLogin { disable() } //HTML-форму
            httpBasic { disable() }
            addFilterBefore(jsonAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
            logout {
                logoutUrl = "/auth/logout"
                deleteCookies("JSESSIONID")
                invalidateHttpSession = true //принудительно закрываем сессию
                logoutSuccessHandler = HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)
            }
        }
        return http.build()
    }

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
                request.session.setAttribute("SPRING_SECURITY_CONTEXT", context) //обязательно

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
        val authProvider = DaoAuthenticationProvider().apply { //сравниваем хеш с паролем из сырого токена
            setUserDetailsService(myDetails) //хеш
            setPasswordEncoder(passwordEncoder()) //декодируем
        }
        return ProviderManager(authProvider)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

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
}
