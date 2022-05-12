package pixelmanga.entities

import javax.persistence.*

@Entity
@Table(name = "chapters")
open class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "sample_id", nullable = false)
    open var sample: Sample? = null

    @Column(name = "image")
    open var image: String? = null
}