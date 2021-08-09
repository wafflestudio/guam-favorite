package waffle.guam.taskmessage.event

data class TaskMessageUpdated(
    val taskMessageId: Long,
) : TaskMessageEvent()
