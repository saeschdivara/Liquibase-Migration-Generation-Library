package foo

import com.thinkinglogic.builder.annotation.Builder
import javax.persistence.*

@Entity
@Builder
class Bank() {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    companion object {
        @JvmStatic
        fun builder() = BankBuilder()
    }
}