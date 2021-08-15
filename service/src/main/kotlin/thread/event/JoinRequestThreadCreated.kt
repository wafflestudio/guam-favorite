package waffle.guam.thread.event

data class JoinRequestThreadCreated(
    val threadId: Long,
) : ThreadEvent()
