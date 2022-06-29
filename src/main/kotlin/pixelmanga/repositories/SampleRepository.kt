package pixelmanga.repositories

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import pixelmanga.entities.Sample

interface SampleRepository : JpaRepository<Sample, Long> {


    @Query("select s from Sample s where s.name = ?1")
    fun findByName(name: String): Sample?



    @Transactional
    @Modifying
    @Query("update Sample s set s.cover = ?1 where s.id = ?2")
    fun updateCoverById(cover: String, id: Long): Int


    @Query("select s from Sample s inner join s.attributes attributes where attributes.name = ?1")
    fun findAllByAttributes_Name(name: String): List<Sample>


    @Query("select s from Sample s where s.name like concat('%', ?1, '%') order by s.name")
    fun findAllByNameContaining(
        title: String,
        pageable: Pageable
    ): Page<Sample>

}