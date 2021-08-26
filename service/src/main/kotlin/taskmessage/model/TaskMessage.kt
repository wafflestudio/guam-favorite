package waffle.guam.taskmessage.model

import waffle.guam.taskmessage.TaskMessageEntity
import java.time.Instant

data class TaskMessage(
    val id: Long,
    val msg: String,
    var status: TaskStatus,
    val taskId: Long,
    val createdAt: Instant,
    val modifiedAt: Instant
) {
    companion object {
        fun TaskMessageEntity.toDomain(): TaskMessage =
            TaskMessage(
                id = id,
                msg = msg,
                status = TaskStatus.valueOf(status),
                taskId = taskId,
                createdAt = createdAt,
                modifiedAt = modifiedAt
            )
    }
}
