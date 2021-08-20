package waffle.guam.task.command

data class DeclineTask(
    val leaderId: Long,
    val guestId: Long,
    val projectId: Long,
) : TaskCommand
