package waffle.guam.task.event

import java.time.Instant

data class TaskAccepted(
    val taskId: Long,
    override val timestamp: Instant = Instant.now(),
) : TaskEvent(timestamp)
