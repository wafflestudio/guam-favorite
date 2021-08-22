package waffle.guam.task.command

data class CompleteTask(
    val projectId: Long
) : TaskCommand
