package waffle.guam.taskMessage.model

import waffle.guam.db.entity.TaskStatus
import java.time.LocalDateTime

data class TaskMessage(
    val id: Long,
    val msg: String,
    var status: TaskStatus,
    val taskId: Long,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
)
