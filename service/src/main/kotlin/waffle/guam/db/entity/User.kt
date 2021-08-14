package waffle.guam.db.entity

import java.time.Instant
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.OneToMany
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

    @Column(name = "device_token")
    var deviceId: String? = null,

    @Enumerated(EnumType.STRING)
    var status: Status = Status.ACTIVE,

    var nickname: String = "",

    var skills: String? = null,

    var githubUrl: String? = null,

    var blogUrl: String? = null,

    var introduction: String? = null,

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "image_id")
    var image: ImageEntity? = null,

    @OneToMany(mappedBy = "userId")
    val tasks: Set<TaskProjectView> = emptySet(),

    val createdAt: Instant = Instant.now(),

    @Column(name = "modified_at")
    var updatedAt: Instant = createdAt,
)

enum class Status {
    ACTIVE, INACTIVE, SUSPENDED
}
