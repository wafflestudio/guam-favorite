package waffle.guam.thread.command

data class RemoveNoticeThread(
    val projectId: Long,
    val userId: Long,
) : ThreadCommand
