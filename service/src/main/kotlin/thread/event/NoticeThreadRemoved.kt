package waffle.guam.thread.event

data class NoticeThreadRemoved(
    val projectId: Long
) : ThreadEvent()
