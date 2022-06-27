package pixelmanga.entities

import org.hibernate.validator.constraints.Range
import javax.persistence.*

@Entity
@Table(name = "rate")
open class Rate {

    @Range(min = 0, max = 10)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "rate", nullable = false)
    open var rating: Int? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    open var user: User? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sample_id", nullable = false)
    open var sample: Sample? = null
}