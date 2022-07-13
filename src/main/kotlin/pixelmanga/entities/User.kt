package pixelmanga.entities

import org.springframework.web.multipart.MultipartFile
import java.sql.Date
import javax.persistence.*
import javax.validation.constraints.Email

@Entity
@Table(name = "users")
open class User : Pathable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Email
    @Column(name = "email", nullable = false, unique = true)
    open var email: String? = null

    @Column(name = "user_name", nullable = false, unique = true)
    open var username: String? = null

    @Column(name = "password", nullable = false)
    open var password: String? = null

    @Column(name = "birth_date")
    open var birthDate: Date? = null

    @Column(name = "avatar")
    open var avatar: String? = null

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    open var roles: MutableSet<Role> = mutableSetOf()

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_favorite_samples",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "samples_id")]
    )
    open var favoriteSamples: MutableSet<Sample> = mutableSetOf()
    override fun setPersonalizedImageName(image: MultipartFile) {
        val regex = """\s|\*|"|\?|\\|>|/|<|:|\|""".toRegex()
        avatar = "${regex.replace(username as String, "_")}-avatar.${image.contentType?.split("/")?.last()}"
    }

    @Transient
    override fun path(): String? {
        return "./resources/images/users/$id"
    }

    override fun existsImage(): Boolean = avatar != null

    @Transient
    override fun imagePath(): String {
        return if (existsImage()) {
            path()+"/"+avatar
        } else {
            "./resources/images/defaults/default-user-avatar.png"
        }
    }
}