package pixelmanga.repositories;

import org.springframework.data.jpa.repository.JpaRepository
import pixelmanga.entities.Chapter

interface ChapterRepository : JpaRepository<Chapter, Long> {
}