package waffle.guam.thread.event

import java.time.Instant

data class ThreadDeleted(
    val threadId: Long,
    val projectId: Long,
    override val timestamp: Instant = Instant.now(),
) : ThreadEvent(timestamp)
