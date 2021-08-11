package waffle.guam.comment.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.comment.CommentEntity

data class CreateComment(
    val threadId: Long,
    val userId: Long,
    val content: String?,
    val imageFiles: List<MultipartFile>?,
) : CommentCommand {
//    init {
//        TypeCheck.validChatInput(content, imageFiles)
//    }
    fun toEntity() = CommentEntity(threadId = threadId, userId = userId, content = content)
}
