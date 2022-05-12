package pixelmanga.entities

import java.sql.Date
import javax.persistence.*
import javax.validation.constraints.FutureOrPresent

@Entity
@Table(name = "samples")
open class Sample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "name", nullable = false, unique = true)
    open var name: String? = null

    @Column(name = "synopsis", nullable = false)
    open var synopsis: String? = null

    @FutureOrPresent
    @Column(name = "publication_date")
    open var publicationDate: Date? = null

    @OneToMany(mappedBy = "sample", cascade = [CascadeType.ALL], orphanRemoval = true)
    open var types: MutableList<Type> = mutableListOf()

    @ManyToMany
    @JoinTable(
        name = "sample_attributes",
        joinColumns = [JoinColumn(name = "sample_id")],
        inverseJoinColumns = [JoinColumn(name = "attributes_id")]
    )
    open var attributes: MutableSet<Attribute> = mutableSetOf()

    @Column(name = "cover")
    open var cover: String? = null
}