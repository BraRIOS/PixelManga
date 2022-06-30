package pixelmanga.repositories;

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pixelmanga.entities.UserSamplesList

interface UserSamplesListRepository : JpaRepository<UserSamplesList, Long> {


    @Query("select u from UserSamplesList u where u.user.username = ?1")
    fun findByUser_Username(username: String): List<UserSamplesList>

}