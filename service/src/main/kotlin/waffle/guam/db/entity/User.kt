package waffle.guam.db.entity

import java.time.Instant
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.OneToOne
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

    var deviceId: String? = null,

    var status: Status = Status.ACTIVE,

    var nickname: String = "",

    var skills: String? = null,

    var githubUrl: String? = null,

    var blogUrl: String? = null,

    var introduction: String? = null,

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "image_id")
    var image: ImageEntity? = null,

    val createdAt: Instant = Instant.now(),

    var updatedAt: Instant = createdAt,
)

enum class Status {
    ACTIVE, INACTIVE, SUSPENDED
}
