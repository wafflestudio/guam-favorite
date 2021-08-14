package waffle.guam.event

import java.time.Instant

data class CommentCreatedEvent(
    val projectId: Long,
    val commentId: Long,
    val threadCreatorId: Long,
    val commentCreatorId: Long,
    val content: String,
    override val timestamp: Instant = Instant.now()
) : ThreadEvent(timestamp)
