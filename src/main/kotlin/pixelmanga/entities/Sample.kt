package pixelmanga.entities

import org.hibernate.Hibernate
import org.springframework.web.multipart.MultipartFile
import java.sql.Date
import javax.persistence.*

@Entity
@Table(name = "samples")
open class Sample: Pathable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "name", nullable = false)
    open var name: String? = null

    @Column(name = "synopsis", length = 2000)
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
    override fun path(): String? {
        val type = attributes.first { attribute -> attribute.type?.name == "tipo de libro" }.name
        return "./resources/images/samples/$type/$id"
    }

    @Transient
    override fun imagePath(): String {
        return if (existsImage()) {
            path()+"/"+cover
        } else {
            "./resources/images/defaults/no-cover.png"
        }
    }

    @Transient
    override fun existsImage(): Boolean = cover != null

    @Transient
    override fun setPersonalizedImageName(image: MultipartFile) {
        val regex = """\s|\*|"|\?|\\|>|/|<|:|\|""".toRegex()
        cover = "${regex.replace(name as String, "_")}-cover.${image.contentType?.split("/")?.last()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Sample

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}