package com.example.pixelmanga.security

import com.example.pixelmanga.entities.User
import com.example.pixelmanga.repositories.AdminRepository
import com.example.pixelmanga.repositories.AuthorRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


class CustomUserDetails(user: User) :UserDetails {

    private val user: User
    private lateinit var adminRepo: AdminRepository
    private lateinit var authorRepo: AuthorRepository

    init {
        this.user = user
    }

    override fun getAuthorities(): MutableCollection< GrantedAuthority> {
        val authorities: MutableCollection<GrantedAuthority> = ArrayList()
        if (isAdmin())
            authorities.add(SimpleGrantedAuthority("ADMIN"))
        if (isAuthor())
            authorities.add(SimpleGrantedAuthority("AUTHOR"))

        return authorities
    }

    private fun isAdmin(): Boolean {
        return adminRepo.existsByUser_Id(user.id)
    }

    private fun isAuthor(): Boolean {
        return authorRepo.existsByUser_Id(user.id)
    }

    override fun getPassword(): String? {
        return user.password
    }

    override fun getUsername(): String? {
        return user.username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}