package waffle.guam.taskmessage.model

import waffle.guam.taskmessage.db.TaskMessageEntity
import java.time.Instant

data class TaskMessage(
    val id: Long,
    val msg: String,
    var status: String,
    val taskId: Long,
    val createdAt: Instant,
    val modifiedAt: Instant
) {
    companion object {
        fun TaskMessageEntity.toDomain() = TaskMessage(
            id = id,
            msg = msg,
            status = status.name,
            taskId = taskId,
            createdAt = createdAt,
            modifiedAt = modifiedAt
        )
    }
}
