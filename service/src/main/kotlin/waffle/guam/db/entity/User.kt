package waffle.guam.db.entity

import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "users")
@Entity
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val firebaseUid: String,

    val status: Status = Status.ACTIVE,

    val name: String,

    val imageUrl: String? = null,

    val skills: String? = null,

    val githubUrl: String? = null,

    val blogUrl: String? = null,

    val introduction: String? = null,

    val createdAt: Instant = Instant.now(),

    val updatedAt: Instant = createdAt,
)

enum class Status {
    ACTIVE, INACTIVE, SUSPENDED
}
