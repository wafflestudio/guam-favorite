package waffle.guam.thread.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.thread.ThreadEntity
import waffle.guam.thread.model.ThreadType
import waffle.guam.util.TypeCheck

data class CreateThread(
    val projectId: Long,
    val userId: Long,
    val content: String?,
    val imageFiles: List<MultipartFile>?,
) : ThreadCommand {
    init {
        TypeCheck.validChatInput(content, imageFiles)
    }
    fun toEntity() = ThreadEntity(projectId = projectId, userId = userId, content = content ?: "", type = ThreadType.NORMAL.name)
}
