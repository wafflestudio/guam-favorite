package waffle.guam.task.event

import java.time.Instant

data class TaskCreated(
    val taskId: Long,
    override val timestamp: Instant = Instant.now()
) : TaskEvent(timestamp)