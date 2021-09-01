package waffle.guam.comment.command

data class DeleteCommentImage(
    val imageId: Long,
    val commentId: Long,
    val userId: Long,
) : CommentCommand
