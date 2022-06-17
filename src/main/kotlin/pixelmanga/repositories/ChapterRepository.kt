package pixelmanga.repositories;

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import pixelmanga.entities.Chapter

interface ChapterRepository : JpaRepository<Chapter, Long> {

    @Transactional
    @Modifying
    @Query("update Chapter c set c.image = ?1 where c.id = ?2")
    fun updateImagePathById(image: String, id: Long): Int


    @Query("select c from Chapter c where c.sample.id = ?1")
    fun findBySample_Id(id: Long): List<Chapter>


    @Query("select count(c) from Chapter c where c.sample.id = ?1")
    fun countBySample_Id(id: Long): Long


    @Query("select c from Chapter c where c.sample.name = ?1")
    fun findBySample_Name(name: String): Chapter


    @Query("select c from Chapter c where c.number = ?1")
    fun findByNumber(number: Long): Chapter

}