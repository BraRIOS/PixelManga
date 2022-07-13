package pixelmanga.entities

import java.nio.file.Paths
import javax.persistence.*
import kotlin.io.path.exists

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
    open var name: String? = null

    @Column(name = "list_description")
    open var description: String? = null


    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_samples_list_samples",
        joinColumns = [JoinColumn(name = "user_samples_list_id")],
        inverseJoinColumns = [JoinColumn(name = "samples_id")]
    )
    open var samples: MutableSet<Sample> = mutableSetOf()

    @Transient
    open fun coverPath(): String {
        return if (Paths.get(path() as String).exists()) {
            path()+"/cover"
        } else {
            "./resources/images/defaults/no-cover.png"
        }
    }

    @Transient
    open fun path(): String? {
        return if (id != null) {
            "./resources/images/lists/$id"
        } else {
            null
        }
    }
}