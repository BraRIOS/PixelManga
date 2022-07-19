package pixelmanga.entities

import org.springframework.web.multipart.MultipartFile
import java.nio.file.Paths
import javax.persistence.*
import kotlin.io.path.exists

@Entity
@Table(name = "user_samples_list")
open class UserSamplesList:Pathable {
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

    @Column(name = "is_public")
    open var isPublic: Boolean? = null

    @ManyToMany
    @JoinTable(
        name = "user_samples_list_followers",
        joinColumns = [JoinColumn(name = "user_samples_list_id")],
        inverseJoinColumns = [JoinColumn(name = "followers_id")]
    )
    open var followers: MutableCollection<User> = mutableListOf()

    @Column(name = "cover")
    open var cover: String? = null

    override fun setPersonalizedImageName(image: MultipartFile) {
        val regex = """\s|\*|"|\?|\\|>|/|<|:|\|""".toRegex()
        cover = "${regex.replace(name as String, "_")}-cover.${image.contentType?.split("/")?.last()}"

    }

    @Transient
    override fun path(): String? {
        return if (id != null) {
            "./resources/images/lists/$id"
        } else {
            null
        }
    }

    override fun imagePath(): String {
        return if (Paths.get(path() as String).exists()) {
            path()+"/"+cover
        } else {
            "./resources/images/defaults/no-cover.png"
        }
    }

    override fun existsImage(): Boolean {
        return Paths.get(imagePath()).exists()
    }
}