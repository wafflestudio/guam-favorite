package waffle.guam.comment.command

data class DeleteComment(
    val commentId: Long,
    val userId: Long
) : CommentCommand
