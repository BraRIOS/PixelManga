package pixelmanga.entities

import javax.persistence.*

@Entity
@Table(name = "chapters")
open class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "title")
    open var title: String? = null

    @ManyToOne(optional = false)
    @JoinColumn(name = "sample_id", nullable = false)
    open var sample: Sample? = null

    @Column(name = "number")
    open var number: Long? = null

    @ElementCollection
    @CollectionTable(name = "chapters_images", joinColumns = [JoinColumn(name = "chapter_id")])
    @Column(name = "image")
    open var images: MutableList<String> = mutableListOf()

    @Transient
    open fun path(): String {
        return if (id != null) {
            "${sample?.path()}/chapters/$number"
        } else {
            ""
        }
    }

    @Transient
    open fun imagesPathList(): List<String> {
        return images.map { "${path()}/$it" }
    }
}