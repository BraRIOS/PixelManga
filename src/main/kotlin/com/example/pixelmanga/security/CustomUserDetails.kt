package com.example.pixelmanga.security

import com.example.pixelmanga.entities.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


class CustomUserDetails(user: User) : UserDetails {

    private val user: User

    init {
        this.user = user
    }

    override fun getAuthorities(): MutableCollection< GrantedAuthority>? {
        return null
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