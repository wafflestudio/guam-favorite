package waffle.guam.task.command

import waffle.guam.db.entity.Position
import waffle.guam.db.entity.UserState

data class CreateTask(
    val projectId: Long,
    val position: Position,
    val userState: UserState
)
