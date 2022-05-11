package pixelmanga.repositories

import pixelmanga.entities.Sample
import org.springframework.data.jpa.repository.JpaRepository

interface SampleRepository : JpaRepository<Sample, Long> {
}