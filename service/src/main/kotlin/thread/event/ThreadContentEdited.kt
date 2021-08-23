package waffle.guam.thread.event

import java.time.Instant

data class ThreadContentEdited(
    val threadId: Long,
    val editedContent: String,
    override val timestamp: Instant = Instant.now(),
) : ThreadEvent(timestamp)
