package waffle.guam.comment.event

data class CommentDeleted(
    val commentId: Long,
    val threadId: Long,
) : CommentEvent()
