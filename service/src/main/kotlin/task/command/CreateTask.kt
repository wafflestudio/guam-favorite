package waffle.guam.task.command

import waffle.guam.task.model.Position

data class CreateTask(
    val userId: Long,
    val projectId: Long,
    val position: Position,
) : TaskCommand
