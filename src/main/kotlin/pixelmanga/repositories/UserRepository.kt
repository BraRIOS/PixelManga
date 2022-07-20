package pixelmanga.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pixelmanga.entities.User

interface UserRepository : JpaRepository<User, Long> {
    @Query("select u from User u where u.username = ?1")
    fun findByUsername(username: String): User?

    @Query("select u from User u where u.email = ?1")
    fun findByEmail(email: String): User?
}