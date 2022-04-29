package com.example.pixelmanga.repositories;

import com.example.pixelmanga.entities.Admin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface AdminRepository : JpaRepository<Admin, Long> {

    @Query("select (count(a) > 0) from Admin a where a.user.id = ?1")
    fun existsByUser_Id(id: Long?): Boolean

}