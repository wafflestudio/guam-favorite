package waffle.guam.task.command

import waffle.guam.task.model.PositionQuota

data class IncOrDecProjectTasks(
    val projectId: Long,
    val userId: Long,
    val quotas: List<PositionQuota>,
) : TaskCommand
