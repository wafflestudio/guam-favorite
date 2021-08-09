package waffle.guam.task.event

import waffle.guam.task.db.UserState
import java.time.Instant

data class TaskUserStateUpdated(
    val taskIds: List<Long>,
    val newState: UserState,
    override val timestamp: Instant = Instant.now()
) : TaskEvent(timestamp)
