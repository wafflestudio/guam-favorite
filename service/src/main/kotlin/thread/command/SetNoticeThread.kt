package waffle.guam.thread.command

data class SetNoticeThread(
    val projectId: Long,
    val threadId: Long?,
    val userId: Long,
) : ThreadCommand
