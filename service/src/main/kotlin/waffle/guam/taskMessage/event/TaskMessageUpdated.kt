package waffle.guam.taskMessage.event

data class TaskMessageUpdated(
    val taskMessageId: Long,
) : TaskMessageEvent()
