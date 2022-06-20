package pixelmanga.entities

import javax.persistence.*

@Entity
@Table(name = "chapters")
open class Chapter {
    lateinit var imagePath: String

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "sample_id", nullable = false)
    open var sample: Sample? = null

    @Column(name = "image", nullable = false)
    open var image: String? = null

    @Column(name = "number", nullable = false, unique = true)
    open var number: Long? = null
}