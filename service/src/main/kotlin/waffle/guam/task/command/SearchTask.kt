package waffle.guam.task.command

import waffle.guam.db.entity.Position
import waffle.guam.db.entity.UserState

data class SearchTask(
    val userId: Long?,
    val projectId: Long?,
    val userStates: List<UserState>,
    val positions: List<Position>,
)
