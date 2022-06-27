package pixelmanga.entities

import javax.persistence.*

@Entity
@Table(name = "user_samples_list")
open class UserSamplesList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_id", nullable = false)
    open var user: User? = null

    @Column(name = "list_name", nullable = false)
    open var listName: String? = null

    @Column(name = "list_description")
    open var listDescription: String? = null

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_samples_list_samples",
        joinColumns = [JoinColumn(name = "user_samples_list_id")],
        inverseJoinColumns = [JoinColumn(name = "samples_id")]
    )
    open var samples: MutableSet<Sample> = mutableSetOf()
}