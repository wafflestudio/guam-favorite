package waffle.guam.taskmessage.command

import waffle.guam.db.entity.TaskMessage
import waffle.guam.db.entity.TaskStatus

data class CreateTaskMessage(
    val taskId: Long,
    val msg: String,
    val status: TaskStatus
) : TaskMessageCommand {
    fun toEntity(): TaskMessage =
        TaskMessage(
            msg = msg,
            status = status,
            taskId = taskId
        )
}
