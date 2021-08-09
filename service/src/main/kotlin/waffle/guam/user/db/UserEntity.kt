package waffle.guam.user.db

import waffle.guam.db.entity.ImageEntity
import waffle.guam.task.db.TaskEntity
import java.time.Instant
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

    val deviceId: String? = null,

    @Enumerated(value = EnumType.STRING)
    var status: UserStatus = UserStatus.ACTIVE,

    var nickname: String = "",

    var skills: String? = null,

    var githubUrl: String? = null,

    var blogUrl: String? = null,

    var introduction: String? = null,

    @OneToOne
    @JoinColumn(name = "image_id")
    var image: ImageEntity? = null,

    @OneToMany(mappedBy = "user")
    val tasks: Set<TaskEntity> = emptySet(),

    val createdAt: Instant = Instant.now(),

    var updatedAt: Instant = createdAt,
)
