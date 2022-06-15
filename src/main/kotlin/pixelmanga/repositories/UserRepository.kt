package pixelmanga.repositories

import pixelmanga.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface UserRepository : JpaRepository<User, Long> {
    @Query("select u from User u where u.username = ?1")
    fun findByUsername(username: String): User

    @Transactional
    @Modifying
    @Query("update User u set u.icon = ?1 where u.id = ?2")
    fun updateIconById(icon: String, id: Long): Int
}