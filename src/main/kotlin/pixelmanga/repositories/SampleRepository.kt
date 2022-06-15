package pixelmanga.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import pixelmanga.entities.Sample

interface SampleRepository : JpaRepository<Sample, Long> {
    @Transactional
    @Modifying
    @Query("update Sample s set s.cover = ?1 where s.id = ?2")
    fun updateCoverPathById(cover: String, id: Long): Int
}