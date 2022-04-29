package com.example.pixelmanga.repositories;

import com.example.pixelmanga.entities.Author
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface AuthorRepository : JpaRepository<Author, Long> {

    @Query("select (count(a) > 0) from Author a where a.user.id = ?1")
    fun existsByUser_Id(id: Long?): Boolean

}