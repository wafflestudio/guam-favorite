package waffle.guam.taskmessage.command

import waffle.guam.db.entity.TaskStatus

data class UpdateTaskMessage(
    val msgId: Long,
    val msg: String?,
    val status: TaskStatus?
) : TaskMessageCommand
