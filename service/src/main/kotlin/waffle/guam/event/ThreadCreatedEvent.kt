package waffle.guam.event

import java.time.Instant

data class ThreadCreatedEvent(
    val projectId: Long,
    val threadId: Long,
    val creatorId: Long,
    val content: String,
    override val timestamp: Instant = Instant.now(),
) : ThreadEvent(timestamp)
