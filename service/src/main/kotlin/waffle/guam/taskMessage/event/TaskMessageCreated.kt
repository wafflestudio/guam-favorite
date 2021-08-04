package waffle.guam.taskMessage.event

data class TaskMessageCreated(
    val taskId: Long,
    val taskMessageId: Long,
) : TaskMessageEvent()
