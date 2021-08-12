package waffle.guam.project.event

import java.time.Instant

class ProjectDeleted(
    val projectId: Long,
    val projectTitle: String,
    override val timestamp: Instant = Instant.now()
) : ProjectEvent(timestamp)
