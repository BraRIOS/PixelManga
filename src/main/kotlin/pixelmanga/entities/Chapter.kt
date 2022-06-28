package pixelmanga.entities

import javax.persistence.*

@Entity
@Table(name = "chapters")
open class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "sample_id", nullable = false)
    open var sample: Sample? = null

    @Column(name = "image", nullable = false)
    open var image: String? = null

    @Column(name = "number", nullable = false)
    open var number: Long? = null

    @Transient
    open fun imagePath(): String? {
        return if (image != null) {
            "${sample?.samplePath()}/chapters/$number/$image"
        } else {
            null
        }
    }
}