package `test-data`.MigrationGeneratorTest.`dataset-011`.new

import com.thinkinglogic.builder.annotation.Builder
import javax.persistence.*

@Builder
@Entity
class User(
        @Column(length = 40, nullable = false)
        var name: String,

        @Column(length = 20, nullable = false, unique = true)
        var username: String,

        @Column(length = 40, nullable = false)
        var email: String,

        @Column(length = 80, nullable = false)
        var password: String,

        @Column(nullable = false)
        var active: Boolean
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @OneToMany
    var accounts: Set<Account> = HashSet()

    companion object {
        @JvmStatic fun builder() = UserBuilder()
    }
}