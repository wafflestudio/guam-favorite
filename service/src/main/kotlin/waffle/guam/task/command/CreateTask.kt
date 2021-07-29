package waffle.guam.task.command

import waffle.guam.db.entity.Position
import waffle.guam.db.entity.TaskEntity
import waffle.guam.db.entity.UserState

data class CreateTask(
    val projectId: Long,
    val position: Position,
    val userState: UserState
) {
    fun toEntity(userId: Long): TaskEntity =
        TaskEntity(
            position = position,
            projectId = projectId,
            userId = userId,
            userState = userState
        )
}
