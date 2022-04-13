package com.example.pixelmanga.repositories

import com.example.pixelmanga.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long> {


    @Query("select u from User u where u.email = ?1")
    fun findByEmail(email: String): User?

}