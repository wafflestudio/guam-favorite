package waffle.guam.comment.event

import waffle.guam.thread.event.ThreadEvent
import java.time.Instant

data class CommentImageDeleted(
    val imageId: Long,
    val commentId: Long,
    val parentThreadId: Long,
    override val timestamp: Instant = Instant.now(),
) : ThreadEvent(timestamp)
