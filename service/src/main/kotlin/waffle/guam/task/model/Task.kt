package waffle.guam.task.model

import waffle.guam.db.entity.UserState
import waffle.guam.model.User
import java.time.LocalDateTime

data class Task(
    val id: Long,
    val position: String,
    val taskMsg: String?,
    val projectId: Long,
    val user: User,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val userState: UserState
)