package waffle.guam.task.model

import waffle.guam.task.TaskEntity
import waffle.guam.task.command.TaskExtraFieldParams
import waffle.guam.taskmessage.model.TaskMessage
import waffle.guam.taskmessage.model.TaskMessage.Companion.toDomain
import waffle.guam.user.model.User
import java.time.Instant

data class Task(
    val id: Long,
    val position: Position,
    val projectId: Long,
    val user: User,
    val userState: UserState,
    val taskMsgs: List<TaskMessage>? = null,
    val createdAt: Instant,
    val modifiedAt: Instant,
) {
    companion object {
        fun TaskEntity.toDomain(extraFieldParams: TaskExtraFieldParams = TaskExtraFieldParams()): Task =
            Task(
                id = id,
                position = Position.valueOf(position),
                projectId = project.id,
                user = User.of(user),
                userState = UserState.valueOf(userState),
                taskMsgs = when (extraFieldParams.withTaskMsgs) {
                    true -> taskMessages.map { it.toDomain() }
                    false -> null
                },
                createdAt = createdAt,
                modifiedAt = modifiedAt
            )
    }
}
