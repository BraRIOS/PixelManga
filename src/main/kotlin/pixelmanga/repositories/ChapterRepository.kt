package pixelmanga.repositories;

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pixelmanga.entities.Chapter

interface ChapterRepository : JpaRepository<Chapter, Long> {


    @Query("select c from Chapter c where c.sample.id = ?1")
    fun findAllBySampleId(id: Long): List<Chapter>


    @Query("select count(c) from Chapter c where c.sample.id = ?1")
    fun countBySampleId(id: Long): Long


    @Query("select c from Chapter c where c.sample.id = ?1 and c.number = ?2")
    fun findBySampleIdAndNumber(id: Long, number: Long): Chapter?

}