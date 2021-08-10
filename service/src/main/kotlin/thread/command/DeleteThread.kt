package waffle.guam.thread.command

data class DeleteThread(
    val threadId: Long,
    val userId: Long,
) : ThreadCommand
