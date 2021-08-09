package waffle.guam.comment.event

data class CommentImageDeleted(
    val commentId: Long,
    val imageId: Long,
) : CommentEvent()
