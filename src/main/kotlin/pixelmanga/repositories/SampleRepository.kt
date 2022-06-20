package pixelmanga.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pixelmanga.entities.Sample

interface SampleRepository : JpaRepository<Sample, Long> {


    @Query("select s from Sample s where s.name = ?1")
    fun findByName(name: String): Sample

}