package waffle.guam.task.command

data class CancelApplyTask(
    val projectId: Long,
    val userId: Long
) : TaskCommand
