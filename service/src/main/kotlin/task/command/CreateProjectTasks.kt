package waffle.guam.task.command

import waffle.guam.task.model.Position
import waffle.guam.task.model.PositionQuota

data class CreateProjectTasks(
    val projectId: Long,
    val leaderId: Long,
    val leaderPosition: Position,
    val quotas: List<PositionQuota>
) : TaskCommand
