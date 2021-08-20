package waffle.guam.task.command

data class LeaveTask(
    val userId: Long,
    val projectId: Long
) : TaskCommand
