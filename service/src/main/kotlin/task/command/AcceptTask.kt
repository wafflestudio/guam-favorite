package waffle.guam.task.command

data class AcceptTask(
    val leaderId: Long,
    val guestId: Long,
    val projectId: Long,
) : TaskCommand
