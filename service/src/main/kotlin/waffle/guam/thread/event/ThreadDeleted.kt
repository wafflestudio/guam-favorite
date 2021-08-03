package waffle.guam.thread.event

data class ThreadDeleted (
    val threadId: Long,
    val parentId: Long,
) : ThreadEvent()