package com.example.pixelmanga.entities

import org.springframework.context.support.BeanDefinitionDsl
import javax.persistence.*
import javax.validation.constraints.Email

@Entity
@Table(name = "user")
open class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Email
    @Column(name = "email", nullable = false, unique = true)
    open var email: String? = null

    @Column(name = "user_name", nullable = false, unique = true)
    open var username: String? = null

    @Column(name = "password", nullable = false)
    open var password: String? = null

    @Column(name = "born_year", nullable = false)
    open var bornYear: Int? = null

    @Column(name = "icon")
    open var icon: String? = null
}