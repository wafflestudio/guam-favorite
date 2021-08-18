package waffle.guam.taskmessage.event

import java.time.Instant

data class TaskMessageCreated(
    val taskId: Long,
    val taskMessageId: Long,
    override val timestamp: Instant = Instant.now()
) : TaskMessageEvent(timestamp)
