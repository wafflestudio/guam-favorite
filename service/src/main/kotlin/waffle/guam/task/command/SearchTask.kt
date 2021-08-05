package waffle.guam.task.command

import waffle.guam.db.entity.Position
import waffle.guam.db.entity.UserState

data class SearchTask(
    val userIds: List<Long>? = null,
    val projectIds: List<Long>? = null,
    val userStates: List<UserState>? = null,
    val positions: List<Position>? = null,
) : TaskCommand
