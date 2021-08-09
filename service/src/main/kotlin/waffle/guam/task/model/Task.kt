package waffle.guam.task.model

import waffle.guam.db.entity.UserState
import waffle.guam.taskmessage.model.TaskMessage
import waffle.guam.user.model.User
import java.time.LocalDateTime

data class Task(
    val id: Long,
    val position: String,
    val projectId: Long,
    val user: User,
    val userState: UserState,
    val taskMsgs: List<TaskMessage>? = null,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
)
