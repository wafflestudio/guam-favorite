package waffle.guam.project.event

import waffle.guam.db.entity.Position
import waffle.guam.project_stack.command.StackIdList
import java.time.Instant

class ProjectCreated(
    val projectId: Long,
    val projectTitle: String,
    val stackIdList: StackIdList,
    val leaderId: Long,
    val leaderPosition: Position,
    override val timestamp: Instant = Instant.now()
) : ProjectEvent(timestamp)
