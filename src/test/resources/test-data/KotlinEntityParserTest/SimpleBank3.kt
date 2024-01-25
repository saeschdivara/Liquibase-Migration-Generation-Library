package foo

import com.thinkinglogic.builder.annotation.Builder
import javax.persistence.*

@Entity
class SimpleBank3(
    @Column(nullable = false)
    var total: Long = 10,

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null
)