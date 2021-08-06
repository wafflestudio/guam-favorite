package waffle.guam.thread.event

data class NoticeThreadSet(
    val threadId: Long,
    val projectId: Long
) : ThreadEvent()
