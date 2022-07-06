package pixelmanga.repositories;

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pixelmanga.entities.Rate

interface RateRepository : JpaRepository<Rate, Long> {

    @Query("select r from Rate r where r.sample.id = ?1")
    fun findAllBySample_Id(id: Long): List<Rate>



    @Query("select r from Rate r where r.user.id = ?1 and r.sample.id = ?2")
    fun findByUser_IdAndSample_Id(userId: Long, sampleId: Long): Rate?


    @Query("select r from Rate r where r.sample.id = ?1")
    fun findBySample_Id(id: Long): Rate

}