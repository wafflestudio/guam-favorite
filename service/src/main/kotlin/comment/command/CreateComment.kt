package waffle.guam.comment.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.comment.CommentEntity
import waffle.guam.user.UserEntity
import waffle.guam.util.TypeCheck

data class CreateComment(
    val threadId: Long,
    val userId: Long,
    val content: String?,
    val imageFiles: List<MultipartFile>?,
) : CommentCommand {
    init {
        TypeCheck.validChatInput(content, imageFiles)
    }
    fun toEntity(user: UserEntity) = CommentEntity(threadId = threadId, user = user, content = content ?: "")
}
