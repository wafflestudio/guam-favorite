package waffle.guam.project.event

import waffle.guam.project_stack.command.StackIdList
import java.time.Instant

class ProjectUpdated(
    val projectId: Long,
    val projectTitle: String,
    val stackIdList: StackIdList,
    override val timestamp: Instant = Instant.now()
) : ProjectEvent(timestamp)
