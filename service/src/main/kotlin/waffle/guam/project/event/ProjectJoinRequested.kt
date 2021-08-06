package waffle.guam.project.event

import waffle.guam.db.entity.Position
import java.time.Instant

class ProjectJoinRequested(
    val projectId: Long,
    val userId: Long,
    val position: Position,
    val introduction: String,
    override val timestamp: Instant = Instant.now()
) : ProjectEvent(timestamp)
