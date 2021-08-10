package waffle.guam.task.model

import waffle.guam.task.TaskEntity
import waffle.guam.task.command.TaskExtraFieldParams
import waffle.guam.taskmessage.model.TaskMessage
import waffle.guam.user.model.User
import java.time.Instant

data class Task(
    val id: Long,
    val position: String,
    val projectId: Long,
    val user: User,
    val userState: UserState,
    val taskMsgs: List<TaskMessage>? = null,
    val createdAt: Instant,
    val modifiedAt: Instant,
) {
    companion object {
        fun TaskEntity.toDomain(extraFieldParams: TaskExtraFieldParams): Task {
            TODO()
        }
    }
}
