package waffle.guam.comment.event

data class CommentCreated(
    val commentId: Long
) : CommentEvent()
