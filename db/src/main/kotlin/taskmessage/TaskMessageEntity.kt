package waffle.guam.taskmessage

import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "task_messages")
@Entity
data class TaskMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val taskId: Long,

    val content: String = "New task msg",

    var status: String,

    val createdAt: Instant = Instant.now(),

    val modifiedAt: Instant = createdAt
)
