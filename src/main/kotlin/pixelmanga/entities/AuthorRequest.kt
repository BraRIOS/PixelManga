package pixelmanga.entities

import java.sql.Date
import javax.persistence.*

@Entity
@Table(name = "author_request")
open class AuthorRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "username", nullable = false)
    open var username: String? = null

    @Column(name = "email", nullable = false)
    open var email: String? = null

    @Column(name = "message", length = 2000)
    open var message: String? = null

    @Column(name = "status", nullable = false)
    open var status: String? = null

    @Column(name = "created_at", nullable = false)
    open var createdAt: Date? = null

    @Column(name = "updated_at")
    open var updatedAt: Date? = null

    @Column(name = "reject_reason", length = 1000)
    open var rejectReason: String? = null

    @Column(name = "updated_by")
    open var updatedBy: String? = null
}