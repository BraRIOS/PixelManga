package pixelmanga.entities

import org.hibernate.validator.constraints.Range
import javax.persistence.*

@Entity
@Table(name = "rate")
open class Rate (user: User, sample: Sample, rate: Int) {
    @Range(min = 0, max = 10)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "rate", nullable = false)
    open var rate: Int? = null

    @ManyToMany
    @JoinTable(
        name = "rate_users",
        joinColumns = [JoinColumn(name = "rate_id")],
        inverseJoinColumns = [JoinColumn(name = "users_id")]
    )
    open var users: MutableSet<User> = mutableSetOf()

    @ManyToMany
    @JoinTable(
        name = "rate_samples",
        joinColumns = [JoinColumn(name = "rate_id")],
        inverseJoinColumns = [JoinColumn(name = "samples_id")]
    )
    open var samples: MutableSet<Sample> = mutableSetOf()
}