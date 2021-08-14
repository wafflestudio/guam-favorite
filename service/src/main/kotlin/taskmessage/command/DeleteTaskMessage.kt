package waffle.guam.taskmessage.command

data class DeleteTaskMessage(
    val userId: Long,
    val msgId: Long
) : TaskMessageCommand
