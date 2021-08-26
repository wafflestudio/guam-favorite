package waffle.guam.task.model

import waffle.guam.task.TaskEntity
import waffle.guam.task.query.TaskExtraFieldParams
import waffle.guam.taskmessage.model.TaskMessage
import waffle.guam.taskmessage.model.TaskMessage.Companion.toDomain
import waffle.guam.taskmessage.model.TaskStatus
import waffle.guam.user.model.User
import java.time.Instant

data class Task(
    val id: Long,
    val position: Position,
    val projectId: Long,
    val user: User?,
    val userState: UserState?,
    val taskMessages: List<TaskMessage>? = null,
    val createdAt: Instant,
    val modifiedAt: Instant,
) {
    companion object {
        fun TaskEntity.toDomain(extraFieldParams: TaskExtraFieldParams = TaskExtraFieldParams()): Task =
            Task(
                id = id,
                position = Position.valueOf(position),
                projectId = project.id,
                user = user?.let { User.of(it) },
                userState = userState?.let { UserState.valueOf(it) },
                taskMessages = when (extraFieldParams.withTaskMsgs) {
                    true -> taskMessages.map { it.toDomain() }.filter { it.status != TaskStatus.DELETED }
                    false -> null
                },
                createdAt = createdAt,
                modifiedAt = modifiedAt
            )
    }
}
