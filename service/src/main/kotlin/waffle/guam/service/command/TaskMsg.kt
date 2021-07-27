package waffle.guam.service.command

import waffle.guam.db.entity.TaskMessage
import waffle.guam.db.entity.TaskStatus

sealed class TaskMsgCommand

data class CreateTaskMsg(
    val taskId: Long,
    val msg: String,
    val status: TaskStatus
) : TaskMsgCommand() {
    fun toEntity(): TaskMessage =
        TaskMessage(
            msg = msg,
            status = status,
            taskId = taskId
        )
}

data class UpdateTaskMsg(
    val msgId: Long,
    val msg: String?,
    val status: TaskStatus?
) : TaskMsgCommand()
