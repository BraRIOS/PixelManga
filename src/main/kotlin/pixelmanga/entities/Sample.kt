package pixelmanga.entities

import java.sql.Date
import javax.persistence.*

@Entity
@Table(name = "samples")
open class Sample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "name", nullable = false)
    open var name: String? = null

    @Column(name = "synopsis", nullable = false, length = 800)
    open var synopsis: String? = null

    @Column(name = "publication_date")
    open var publicationDate: Date? = null

    @ManyToMany(fetch = FetchType.EAGER)
    @OrderBy("name")
    @JoinTable(
        name = "sample_attributes",
        joinColumns = [JoinColumn(name = "sample_id")],
        inverseJoinColumns = [JoinColumn(name = "attributes_id")]
    )
    open var attributes: MutableSet<Attribute> = mutableSetOf()

    @Column(name = "cover")
    open var cover: String? = null

    @Transient
    open fun coverPath(): String? {
        return if (cover != null) {
            samplePath()+"/"+cover
        } else {
            null
        }
    }

    @Transient
    open fun samplePath(): String? {
        return if (id != null) {
            val type = attributes.first { attribute -> attribute.type?.name == "tipo de libro" }.name
            "./resources/images/samples/$type/$id"
        } else {
            null
        }
    }
}