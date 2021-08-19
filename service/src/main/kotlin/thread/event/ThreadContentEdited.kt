package waffle.guam.thread.event

import waffle.guam.thread.ThreadEntity
import java.time.Instant

data class ThreadContentEdited(
    val threadId: Long,
    val thread: ThreadEntity,
    override val timestamp: Instant = Instant.now(),
) : ThreadEvent(timestamp)
