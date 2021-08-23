package waffle.guam.task.event

import java.time.Instant

data class TaskAccepted(
    val projectId: Long,
    val userId: Long,
    override val timestamp: Instant = Instant.now(),
) : TaskEvent(timestamp)
