package waffle.guam.service.command

import waffle.guam.db.entity.ProjectEntity
import waffle.guam.db.entity.TaskMessage
import waffle.guam.db.entity.TaskStatus

sealed class TaskMsg

data class CreateTaskMsg(
    val msg: String?,
    val status: TaskStatus?
): TaskMsg(){
    fun toEntity(taskId: Long): TaskMessage =
        TaskMessage(
            msg = msg,
            status = status,
            taskId = taskId
        )
}
