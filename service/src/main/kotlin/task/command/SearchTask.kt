package waffle.guam.task.command

import waffle.guam.task.model.Position
import waffle.guam.task.model.UserState

data class SearchTask(
    val userIds: List<Long>? = null,
    val projectIds: List<Long>? = null,
    val userStates: List<UserState>? = null,
    val positions: List<Position>? = null,
) : TaskCommand
