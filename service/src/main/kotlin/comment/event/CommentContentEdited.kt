package waffle.guam.comment.event

import waffle.guam.image.ImageEntity
import waffle.guam.thread.event.ThreadEvent
import java.time.Instant

data class CommentContentEdited(
    val commentId: Long,
    val commentContent: String,
    val commentImages: List<ImageEntity>,
    val parentThreadId: Long,
    override val timestamp: Instant = Instant.now(),
) : ThreadEvent(timestamp)
