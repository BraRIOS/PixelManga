package com.example.pixelmanga.entities

import javax.persistence.*

@Entity
@Table(name = "sample_revision")
open class SampleRevision {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "is_approved")
    open var isApproved: Boolean? = null

    @Column(name = "message")
    open var message: String? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "admin_id")
    open var admin: Admin? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "author_id")
    open var author: Author? = null

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "sample_id")
    open var sample: Sample? = null
}