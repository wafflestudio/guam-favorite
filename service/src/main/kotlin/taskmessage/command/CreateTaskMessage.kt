package waffle.guam.taskmessage.command

import waffle.guam.taskmessage.TaskMessageEntity
import waffle.guam.taskmessage.model.TaskStatus

data class CreateTaskMessage(
    val userId: Long,
    val taskId: Long,
    val msg: String,
    val status: TaskStatus
) : TaskMessageCommand {
    fun toEntity(): TaskMessageEntity =
        TaskMessageEntity(
            msg = msg,
            status = status.name,
            taskId = taskId
        )
}
