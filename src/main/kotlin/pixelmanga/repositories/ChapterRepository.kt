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

}