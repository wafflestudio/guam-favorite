package waffle.guam.user

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table

@Table(name = "users")
@Entity
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Lob
    @Column
    val firebaseUid: String,

    val deviceId: String? = null,

    @Enumerated(value = EnumType.STRING)
    val status: String,

    val nickname: String = "",

    val skills: String? = null,

    val githubUrl: String? = null,

    val blogUrl: String? = null,

    val introduction: String? = null,

    val createdAt: Instant = Instant.now(),

    var updatedAt: Instant = createdAt,
)
