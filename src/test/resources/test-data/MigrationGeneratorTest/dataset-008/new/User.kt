package clothing.dressup.accounting.web.app.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*


@Entity
@Table
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var name: String? = null

    @Column(nullable = false, unique = true)
    var email: String? = null

    @Column
    var imageUrl: String? = null

    @Column(nullable = false)
    var emailVerified = false

    @JsonIgnore
    @Column
    var password: String? = null

    @Enumerated(EnumType.STRING)
    var provider: AuthProvider? = null

    @Column
    var providerId: String? = null
}