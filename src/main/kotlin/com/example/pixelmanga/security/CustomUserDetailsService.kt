package com.example.pixelmanga.security

import com.example.pixelmanga.entities.User
import com.example.pixelmanga.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException


class CustomUserDetailsService : UserDetailsService {
    @Autowired
    private val userRepo: UserRepository? = null

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user: User = userRepo?.findByEmail(username) ?: throw UsernameNotFoundException("User not found")
        return CustomUserDetails(user)
    }
}