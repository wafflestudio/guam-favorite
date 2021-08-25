package waffle.guam.task.event

import java.time.Instant

data class TaskApplied(
    val projectId: Long,
    val userId: Long,
    val introduction: String,
    override val timestamp: Instant = Instant.now()
) : TaskEvent(timestamp)
