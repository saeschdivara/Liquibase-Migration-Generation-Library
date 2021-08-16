package foo

import com.thinkinglogic.builder.annotation.Builder
import javax.persistence.*

@Builder
@Entity
class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @OneToMany
    var users: Set<User> = HashSet()

    companion object {
        @JvmStatic fun builder() = UserBuilder()
    }
}