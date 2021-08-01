package waffle.guam.comment.command

import waffle.guam.db.entity.CommentEntity
import waffle.guam.db.entity.ImageEntity
import waffle.guam.db.entity.ImageType

data class DeleteCommentImage(
    val imageId: Long,
    val commentId: Long,
    val type: ImageType = ImageType.COMMENT,
    val userId: Long,
) : CommentCommand {
    var commentContent: String? = null
}