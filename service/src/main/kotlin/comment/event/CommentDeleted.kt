package waffle.guam.comment.event

import waffle.guam.thread.event.ThreadEvent
import java.time.Instant

data class CommentDeleted(
    val commentId: Long,
    val parentThreadId: Long,
    override val timestamp: Instant = Instant.now(),
) : ThreadEvent(timestamp)
