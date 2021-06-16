package clothing.dressup.accounting.web.app.entities

import com.thinkinglogic.builder.annotation.Builder
import java.time.LocalDate
import javax.persistence.*

@Entity
@Builder
class TransactionSchedule(
    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    var timeType: TimeType,

    @Column(nullable = false)
    var month: Int,

    @Column(nullable = false)
    var dayOfMonth: Int,

    @Column(nullable = false)
    var fromDate: LocalDate,

    @Column
    var dueDate: LocalDate?,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    companion object {
        @JvmStatic fun builder() = TransactionScheduleBuilder()
    }
}