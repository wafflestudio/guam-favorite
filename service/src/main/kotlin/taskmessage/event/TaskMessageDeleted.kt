package waffle.guam.taskmessage.event

import java.time.Instant

data class TaskMessageDeleted(
    val taskMessageId: Long,
    override val timestamp: Instant = Instant.now()
) : TaskMessageEvent(timestamp)
