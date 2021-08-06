package waffle.guam.project_stack.event

import java.time.Instant

class ProjectStacksCreated(
    val projectId: Long,
    val stackId: List<Long>,
    override val timestamp: Instant = Instant.now()
) : ProjectStackEvent(timestamp)
