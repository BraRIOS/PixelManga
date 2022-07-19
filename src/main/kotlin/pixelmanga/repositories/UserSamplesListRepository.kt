package pixelmanga.repositories;

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pixelmanga.entities.UserSamplesList

interface UserSamplesListRepository : JpaRepository<UserSamplesList, Long> {


    @Query("select u from UserSamplesList u where u.user.username = ?1")
    fun findAllByUser_Username(username: String): List<UserSamplesList>

    @Query("select u from UserSamplesList u inner join u.followers followers where followers.username = ?1")
    fun findAllByFollowersContaining(username: String): List<UserSamplesList>


    @Query("select u from UserSamplesList u where u.isPublic = true")
    fun findAllByIsPublicTrue(): List<UserSamplesList>

}