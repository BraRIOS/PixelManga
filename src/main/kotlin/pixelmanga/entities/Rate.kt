package pixelmanga.entities

import javax.persistence.*

@Entity
@Table(name = "rates")
open class Rate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "rate", nullable = false)
    open var rating: Int = 0

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    open var user: User? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sample_id", nullable = false)
    open var sample: Sample? = null
}