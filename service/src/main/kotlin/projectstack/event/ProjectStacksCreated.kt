package waffle.guam.projectstack.event

import java.time.Instant

class ProjectStacksCreated(
    val projectId: Long,
    val stackIds: List<Long>,
    override val timestamp: Instant = Instant.now()
) : ProjectStackEvent(timestamp)
