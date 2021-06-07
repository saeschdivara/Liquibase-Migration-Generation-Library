package foo

import com.thinkinglogic.builder.annotation.Builder
import javax.persistence.*

@Entity
@Builder
class BankAccount(
    @Column(length = 40, nullable = false, unique = true)
    var name: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    companion object {
        @JvmStatic
        fun builder() = BankBuilder()
    }
}