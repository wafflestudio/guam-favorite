package waffle.guam.taskmessage.command

import waffle.guam.taskmessage.TaskMessageEntity
import waffle.guam.taskmessage.model.TaskStatus

data class CreateTaskMessage(
    val userId: Long,
    val taskId: Long,
    val messageContent: String,
    val status: TaskStatus
) : TaskMessageCommand {
    fun toEntity(): TaskMessageEntity =
        TaskMessageEntity(
            content = messageContent,
            status = status.name,
            taskId = taskId
        )
}
