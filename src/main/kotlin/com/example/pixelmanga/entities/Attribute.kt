package com.example.pixelmanga.entities

import org.hibernate.Hibernate
import javax.persistence.*

@Entity
@Table(name = "attribute")
open class Attribute {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "name", nullable = false, unique = true)
    open var name: String? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    open var type: Type? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Attribute

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}