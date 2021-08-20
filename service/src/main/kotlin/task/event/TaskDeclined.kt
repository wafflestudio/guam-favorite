package waffle.guam.task.event

import java.time.Instant

data class TaskDeclined(
    val taskId: Long,
    override val timestamp: Instant = Instant.now(),
) : TaskEvent(timestamp)
