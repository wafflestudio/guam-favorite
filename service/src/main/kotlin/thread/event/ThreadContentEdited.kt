package waffle.guam.thread.event

import java.time.Instant

data class ThreadContentEdited(
    val threadId: Long,
    override val timestamp: Instant = Instant.now(),
) : ThreadEvent(timestamp)
