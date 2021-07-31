package waffle.guam.comment.command

import waffle.guam.db.entity.CommentEntity

data class DeleteComment(
    val commentId: Long,
    val userId: Long,
    val targetComment: CommentEntity?
) : CommentCommand