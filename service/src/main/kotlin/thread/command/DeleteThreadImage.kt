package waffle.guam.thread.command

data class DeleteThreadImage(
    val threadId: Long,
    val imageId: Long,
    val userId: Long,
) : ThreadCommand
