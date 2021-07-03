package waffle.guam.service.command

import waffle.guam.db.entity.CommentEntity
import waffle.guam.db.entity.ImageType

sealed class CommentCommand

data class CreateComment(
    val threadId: Long,
    val userId: Long,
    val content: String?,
    val imageUrls: List<String>?
) : CommentCommand() {
    fun toEntity() = CommentEntity(threadId = threadId, userId = userId, content = content)
}

data class EditCommentContent(
    val commentId: Long,
    val userId: Long,
    val content: String
) : CommentCommand()

data class DeleteCommentImage(
    val imageId: Long,
    val commentId: Long,
    val type: ImageType = ImageType.COMMENT,
    val userId: Long,
) : CommentCommand()

data class DeleteComment(
    val commentId: Long,
    val userId: Long,
) : CommentCommand()
