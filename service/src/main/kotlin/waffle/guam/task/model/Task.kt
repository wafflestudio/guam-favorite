package waffle.guam.task.model

import waffle.guam.task.db.TaskEntity
import waffle.guam.taskmessage.model.TaskMessage
import waffle.guam.taskmessage.model.TaskMessage.Companion.toDomain
import waffle.guam.user.model.User
import waffle.guam.user.model.User.Companion.toDomain
import java.time.Instant

data class Task(
    val id: Long,
    val position: String,
    val projectId: Long,
    val user: User,
    val userState: String,
    val taskMessages: List<TaskMessage>? = null,
    val createdAt: Instant,
    val modifiedAt: Instant,
) {
    companion object {
        fun TaskEntity.toDomain(withTaskMsgs: Boolean = false) = Task(
            id = id,
            position = position.name,
            projectId = projectId,
            user = user.toDomain(),
            userState = userState.name,
            taskMessages = when (withTaskMsgs) {
                true -> taskMessages.map { it.toDomain() }
                false -> null
            },
            createdAt = createdAt,
            modifiedAt = modifiedAt
        )
    }
}
