package waffle.guam.comment.event

import waffle.guam.thread.event.ThreadEvent
import java.time.Instant

data class CommentImageDeleted(
    val commentId: Long,
    val imageId: Long,
    override val timestamp: Instant = Instant.now(),
) : ThreadEvent(timestamp)
