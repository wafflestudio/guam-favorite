package waffle.guam.task.command

import waffle.guam.task.TaskEntity
import waffle.guam.task.model.Position
import waffle.guam.task.model.UserState
import waffle.guam.user.UserEntity

data class CreateTask(
    val projectId: Long,
    val position: Position,
    val userState: UserState,
) : TaskCommand {
    fun toEntity(getUser: () -> UserEntity): TaskEntity =
        TaskEntity(
            projectId = projectId,
            position = position.name,
            user = getUser(),
            userState = userState.name
        )
}
