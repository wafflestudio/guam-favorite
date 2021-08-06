package waffle.guam.project_stack.event

import java.time.Instant

class ProjectStacksUpdated(
    val projectId: Long,
    val stackId: List<Long>,
    override val timestamp: Instant = Instant.now()
) : ProjectStackEvent(timestamp)
