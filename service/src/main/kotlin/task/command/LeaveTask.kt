package waffle.guam.task.command

data class LeaveTask(
    val projectId: Long,
    val userId: Long,
) : TaskCommand
