package waffle.guam.task.event

import java.time.Instant

data class TaskCanceled(
    val projectId: Long,
    override val timestamp: Instant = Instant.now(),
) : TaskEvent(timestamp)
