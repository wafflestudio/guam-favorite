package waffle.guam.task.event

import java.time.Instant

data class TaskJoined(
    val taskId: Long,
    override val timestamp: Instant = Instant.now()
) : TaskEvent(timestamp)
