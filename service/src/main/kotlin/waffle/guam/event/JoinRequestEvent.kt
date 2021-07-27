package waffle.guam.event

import java.time.Instant

data class JoinRequestEvent(
    val projectTitle: String,
    val projectUserIds: List<Long>,
    override val timestamp: Instant = Instant.now()
) : ProjectEvent(timestamp)
