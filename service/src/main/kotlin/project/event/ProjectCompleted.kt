package waffle.guam.project.event

import java.time.Instant

data class ProjectCompleted(
    val projectId: Long,
    val projectTitle: String,
    override val timestamp: Instant = Instant.now()
) : ProjectEvent(timestamp)
