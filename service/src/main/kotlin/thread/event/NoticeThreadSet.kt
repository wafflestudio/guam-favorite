package waffle.guam.thread.event

import java.time.Instant

data class NoticeThreadSet(
    val projectId: Long,
    val threadId: Long? = null,
    override val timestamp: Instant = Instant.now(),
) : ThreadEvent(timestamp)
