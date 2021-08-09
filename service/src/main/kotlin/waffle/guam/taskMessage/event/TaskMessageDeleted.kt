package waffle.guam.taskMessage.event

data class TaskMessageDeleted(
    val taskMessageId: Long,
) : TaskMessageEvent()
