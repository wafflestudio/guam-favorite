package waffle.guam.taskmessage.event

data class TaskMessageCreated(
    val taskId: Long,
    val taskMessageId: Long,
) : TaskMessageEvent()
