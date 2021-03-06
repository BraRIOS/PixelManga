package com.example.pixelmanga.entities

import javax.persistence.*

@Entity
@Table(name = "type")
open class Type {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "name", nullable = false)
    open var name: String? = null

    @ManyToOne
    @JoinColumn(name = "sample_id")
    open var sample: Sample? = null
}