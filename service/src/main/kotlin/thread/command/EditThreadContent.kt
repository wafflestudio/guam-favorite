package waffle.guam.thread.command

data class EditThreadContent(
    val threadId: Long,
    val userId: Long,
    val content: String
) : ThreadCommand
