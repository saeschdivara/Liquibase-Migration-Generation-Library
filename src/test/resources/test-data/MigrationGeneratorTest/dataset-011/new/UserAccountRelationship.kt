package `test-data`.MigrationGeneratorTest.`dataset-011`.new

import com.thinkinglogic.builder.annotation.Builder
import java.io.Serializable
import javax.persistence.*

@Embeddable
data class UserAccountRelationshipKey(
    @Column
    var userId: Long,
    @Column
    var accountId: Long
) : Serializable

@Builder
@Entity
class UserAccountRelationship {
    @EmbeddedId
    var id: UserAccountRelationshipKey? = null

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    var user: User? = null

    @ManyToOne
    @MapsId("accountId")
    @JoinColumn(name = "account_id")
    var account: Account? = null
}