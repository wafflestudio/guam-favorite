package waffle.guam.task.command

data class CancelTask(
    val projectId: Long
) : TaskCommand
