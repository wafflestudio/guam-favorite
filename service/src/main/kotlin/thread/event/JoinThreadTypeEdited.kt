package waffle.guam.thread.event

import waffle.guam.thread.model.ThreadType
import java.time.Instant

data class JoinThreadTypeEdited(
    val threadId: Long,
    val type: ThreadType,
    override val timestamp: Instant = Instant.now(),
) : ThreadEvent(timestamp)
