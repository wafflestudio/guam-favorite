package waffle.guam.taskmessage.command

import waffle.guam.taskmessage.model.TaskStatus

data class UpdateTaskMessage(
    val taskMessageId: Long,
    val messageContent: String?,
    val status: TaskStatus?
) : TaskMessageCommand
