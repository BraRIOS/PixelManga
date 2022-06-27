package pixelmanga.entities

import javax.persistence.*
import javax.validation.constraints.Email

@Entity
@Table(name = "users")
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


    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    open var roles: MutableSet<Role> = mutableSetOf()

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_favorite_samples",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "samples_id")]
    )
    open var favoriteSamples: MutableSet<Sample> = mutableSetOf()
}