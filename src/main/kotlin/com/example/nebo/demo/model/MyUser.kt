package com.example.nebo.demo.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDate


@Entity
@Table(name = "users")
data class MyUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(unique = true, nullable = false)
    val email: String,
    @Column(nullable = false, name = "password_hash")
    @JsonIgnore
    val passwordHash: String,
    @Column(nullable = false)
    val name: String,
    @Column(name = "birth_date")
    @JsonIgnore
    val birthDate: LocalDate,
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    var drawings: MutableList<Drawing> = mutableListOf(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "role")
    val roles: Set<String> = setOf("USER"),
    @Column(name = "file_path", length = 2000)
    var filePath: String
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return roles.map { SimpleGrantedAuthority("ROLE_$it") }
    }
    override fun getPassword(): String = passwordHash
    override fun getUsername(): String = email
}
