package waffle.guam.comment.command

import waffle.guam.db.entity.CommentEntity

data class EditCommentContent(
    val commentId: Long,
    val userId: Long,
    val content: String,
) : CommentCommand
