package waffle.guam.task

import waffle.guam.taskmessage.TaskMessageEntity
import waffle.guam.user.UserEntity
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

@Table(name = "tasks")
@Entity
data class TaskEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val projectId: Long,

    val position: String,

    @OneToMany(mappedBy = "taskId")
    val taskMessages: Set<TaskMessageEntity>,

    @OneToOne
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    val userState: String,

    val createdAt: Instant = Instant.now(),

    val modifiedAt: Instant = createdAt,
)
