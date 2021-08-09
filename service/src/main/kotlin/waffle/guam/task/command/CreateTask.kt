package waffle.guam.task.command

import waffle.guam.task.db.Position
import waffle.guam.task.db.UserState

data class CreateTask(
    val projectId: Long,
    val position: Position,
    val userState: UserState
) : TaskCommand
