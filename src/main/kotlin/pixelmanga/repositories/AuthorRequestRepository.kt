package pixelmanga.repositories;

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pixelmanga.entities.AuthorRequest

interface AuthorRequestRepository : JpaRepository<AuthorRequest, Long> {

    @Query("select a from AuthorRequest a where a.username = ?1 order by a.id DESC")
    fun findByUsernameOrderByIdDesc(username: String): List<AuthorRequest>

}