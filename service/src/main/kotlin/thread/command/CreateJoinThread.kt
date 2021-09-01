package waffle.guam.thread.command

import waffle.guam.InvalidRequestException
import waffle.guam.thread.ThreadEntity
import waffle.guam.thread.model.ThreadType

data class CreateJoinThread(
    val projectId: Long,
    val userId: Long,
    val content: String
) : ThreadCommand {
    init {
        if (content.isBlank()) {
            throw InvalidRequestException("입력된 내용이 없습니다.")
        }
    }
    fun toEntity() = ThreadEntity(projectId = projectId, userId = userId, content = content, type = ThreadType.JOIN.name)
}
