package waffle.guam.taskmessage.event

data class TaskMessageDeleted(
    val taskMessageId: Long,
) : TaskMessageEvent()
