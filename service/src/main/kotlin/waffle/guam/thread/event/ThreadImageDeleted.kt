package waffle.guam.thread.event

data class ThreadImageDeleted(
    val threadId: Long,
    val imageId: Long,
) : ThreadEvent()