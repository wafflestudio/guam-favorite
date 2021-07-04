package waffle.guam.service.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.db.entity.ImageType
import waffle.guam.db.entity.ThreadEntity

sealed class ThreadCommand

data class CreateThread(
    val projectId: Long,
    val userId: Long,
    val content: String?,
    val imageFiles: List<MultipartFile>?,
) : ThreadCommand() {
    fun toEntity() = ThreadEntity(projectId = projectId, userId = userId, content = content)
}

data class EditThreadContent(
    val threadId: Long,
    val userId: Long,
    val content: String
) : ThreadCommand()

data class DeleteThreadImage(
    val imageId: Long,
    val threadId: Long,
    val type: ImageType = ImageType.THREAD,
    val userId: Long,
) : ThreadCommand()

data class DeleteThread(
    val threadId: Long,
    val userId: Long,
) : ThreadCommand()
