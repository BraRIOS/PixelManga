package com.example.pixelmanga.entities

import javax.persistence.*

@Entity
@Table(name = "author")
open class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "user_id", unique = true)
    open var user: User? = null
}