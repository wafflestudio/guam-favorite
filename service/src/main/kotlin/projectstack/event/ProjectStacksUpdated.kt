package waffle.guam.projectstack.event

import java.time.Instant

class ProjectStacksUpdated(
    val projectId: Long,
    val stackIds: List<Long>,
    override val timestamp: Instant = Instant.now()
) : ProjectStackEvent(timestamp)
