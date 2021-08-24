package waffle.guam.task.event

import java.time.Instant

data class TaskCreated(
    val projectId: Long,
    override val timestamp: Instant = Instant.now()
) : TaskEvent(timestamp)
