package waffle.guam.thread.event

data class ThreadDeleted(
    val threadId: Long,
    val projectId: Long,
) : ThreadEvent()
