package waffle.guam.event

import java.time.Instant

data class JoinResultEvent(
    val projectTitle: String,
    val targetUserId: Long,
    val accepted: Boolean,
    override val timestamp: Instant = Instant.now()
) : ProjectEvent(timestamp)
