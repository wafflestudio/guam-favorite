package waffle.guam.thread.event

import java.time.Instant

data class ThreadImageDeleted(
    val threadId: Long,
    val deletedImageId: Long,
    val projectId: Long,
    override val timestamp: Instant = Instant.now(),
) : ThreadEvent(timestamp)
