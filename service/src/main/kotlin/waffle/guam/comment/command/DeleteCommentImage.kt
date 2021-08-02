package waffle.guam.comment.command

import waffle.guam.db.entity.ImageType

data class DeleteCommentImage(
    val imageId: Long,
    val commentId: Long,
    val type: ImageType = ImageType.COMMENT,
    val userId: Long,
) : CommentCommand
