package pixelmanga.repositories;

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pixelmanga.entities.Attribute

interface AttributeRepository : JpaRepository<Attribute, Long> {


    @Query("select a from Attribute a where a.type.name = ?1")
    fun findByType_Name(name: String): List<Attribute>

}