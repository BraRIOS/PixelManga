package pixelmanga.repositories;

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pixelmanga.entities.Rate

interface RateRepository : JpaRepository<Rate, Long> {

    @Query("select r from Rate r inner join r.samples samples where samples.id = ?1")
    fun findAllBySample_Id(id: Long): Iterable<Rate>

}