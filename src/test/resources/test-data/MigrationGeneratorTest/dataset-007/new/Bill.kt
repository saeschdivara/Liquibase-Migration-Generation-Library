package clothing.dressup.accounting.web.app.entities

import clothing.dressup.accounting.web.authentication.entity.User
import com.thinkinglogic.builder.annotation.Builder
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import javax.persistence.*

@Entity
@Builder
class Bill(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var account: BankAccount,

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var schedule: TransactionSchedule,

    @Column(nullable = false)
    var amount: Double,

    @Column(nullable = false, length = 2048)
    var description: String,

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null
) {

    companion object {
        @JvmStatic fun builder() = BillBuilder()
    }
}