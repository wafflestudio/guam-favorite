package waffle.guam.thread.event

data class NoticeThreadSet(
    val projectId: Long,
    val threadId: Long? = null,
) : ThreadEvent()
