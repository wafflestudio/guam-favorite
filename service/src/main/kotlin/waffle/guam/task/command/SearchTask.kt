package waffle.guam.task.command

import waffle.guam.db.entity.Position
import waffle.guam.db.entity.UserState

data class SearchTask(
    val userId: Long? = null,
    val projectId: Long? = null,
    val userStates: List<UserState> = emptyList(),
    val positions: List<Position> = emptyList(),
)
