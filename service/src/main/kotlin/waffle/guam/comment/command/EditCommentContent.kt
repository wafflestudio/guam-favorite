package waffle.guam.comment.command

data class EditCommentContent(
    val commentId: Long,
    val userId: Long,
    val content: String,
) : CommentCommand
