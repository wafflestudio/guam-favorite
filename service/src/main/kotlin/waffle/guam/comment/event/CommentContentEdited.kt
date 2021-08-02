package waffle.guam.comment.event

data class CommentContentEdited(
    val commentId: Long,
) : CommentEvent()
