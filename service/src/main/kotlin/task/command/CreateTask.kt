package waffle.guam.task.command

import waffle.guam.task.model.Position
import waffle.guam.task.model.UserState

data class CreateTask(
    val projectId: Long,
    val position: Position,
    val userState: UserState
) : TaskCommand
