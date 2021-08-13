package foo

import clothing.dressup.accounting.web.authentication.entity.User
import com.thinkinglogic.builder.annotation.Builder
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import javax.persistence.*

@Entity
@Builder
class Bill(
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