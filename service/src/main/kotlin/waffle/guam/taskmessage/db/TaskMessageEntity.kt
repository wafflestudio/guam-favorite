package waffle.guam.taskmessage.db

import waffle.guam.db.entity.TaskStatus
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Table
@Entity
data class TaskMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val msg: String = "New task msg",

    val status: TaskStatus = TaskStatus.ONGOING,

    val taskId: Long,

    val createdAt: Instant = Instant.now(),

    val modifiedAt: Instant = createdAt
)
