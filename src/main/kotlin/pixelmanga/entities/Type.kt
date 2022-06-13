package pixelmanga.entities

import javax.persistence.*

@Entity
@Table(name = "types")
open class Type {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "name", nullable = false)
    open var name: String? = null
}