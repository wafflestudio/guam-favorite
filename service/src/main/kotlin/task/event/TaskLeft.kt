package waffle.guam.task.event

import java.time.Instant

data class TaskLeft(
    val userId: Long,
    val projectId: Long,
    override val timestamp: Instant = Instant.now(),
) : TaskEvent(timestamp)
