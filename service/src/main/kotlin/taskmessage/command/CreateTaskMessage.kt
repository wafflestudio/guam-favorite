package waffle.guam.taskmessage.command

import waffle.guam.taskmessage.model.TaskStatus

data class CreateTaskMessage(
    val taskId: Long,
    val msg: String,
    val status: TaskStatus
) : TaskMessageCommand {
//    fun toEntity(): TaskMessage =
//        TaskMessage(
//            msg = msg,
//            status = status,
//            taskId = taskId
//        )
}
