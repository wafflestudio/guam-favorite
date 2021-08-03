package waffle.guam.thread.event

data class ThreadContentEdited (
    val threadId: Long,
) : ThreadEvent()