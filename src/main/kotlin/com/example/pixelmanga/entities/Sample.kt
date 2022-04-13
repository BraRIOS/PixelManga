package com.example.pixelmanga.entities

import java.time.LocalDate
import javax.persistence.*
import javax.validation.constraints.FutureOrPresent

@Entity
@Table(name = "sample")
open class Sample {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "name", nullable = false, unique = true)
    open var name: String? = null

    @Column(name = "synposis", nullable = false)
    open var synposis: String? = null

    @FutureOrPresent
    @Column(name = "publication_date")
    open var publication_date: LocalDate? = null

    @OneToMany(mappedBy = "sample", cascade = [CascadeType.ALL], orphanRemoval = true)
    open var types: MutableList<Type> = mutableListOf()

    @ManyToMany
    @JoinTable(
        name = "sample_attributes",
        joinColumns = [JoinColumn(name = "sample_id")],
        inverseJoinColumns = [JoinColumn(name = "attributes_id")]
    )
    open var attributes: MutableSet<Attribute> = mutableSetOf()

    @Column(name = "cover")
    open var cover: String? = null
}