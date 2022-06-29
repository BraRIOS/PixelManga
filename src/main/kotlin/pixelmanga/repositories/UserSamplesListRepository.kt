package pixelmanga.repositories;

import org.springframework.data.jpa.repository.JpaRepository
import pixelmanga.entities.UserSamplesList

interface UserSamplesListRepository : JpaRepository<UserSamplesList, Long> {


    fun findByUser_Id(id: Long): List<UserSamplesList>

}