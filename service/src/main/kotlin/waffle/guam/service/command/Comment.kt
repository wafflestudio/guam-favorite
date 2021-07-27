package waffle.guam.service.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.db.entity.CommentEntity
import waffle.guam.db.entity.ImageType
import waffle.guam.util.TypeCheck

sealed class CommentCommand

data class CreateComment(
    val threadId: Long,
    val userId: Long,
    val content: String?,
    val imageFiles: List<MultipartFile>?,
) : CommentCommand() {
    init {
        TypeCheck.validChatInput(content, imageFiles)
    }
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
