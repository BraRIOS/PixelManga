package pixelmanga.repositories;

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pixelmanga.entities.Role

interface RoleRepository : JpaRepository<Role, Long> {
    @Query("select r from Role r where r.name = ?1")
    fun findByName(name: String): Role
}