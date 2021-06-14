package foo

import com.thinkinglogic.builder.annotation.Builder
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import javax.persistence.*

@Entity
@Builder
class BankAccount(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bank_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    var bank: Bank?
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    companion object {
        @JvmStatic fun builder() = BankAccountBuilder()
    }
}