package waffle.guam.thread.command

import waffle.guam.thread.ThreadEntity
import waffle.guam.thread.model.ThreadType

data class CreateJoinThread(
    val projectId: Long,
    val userId: Long,
    val content: String
) : ThreadCommand {
    fun toEntity() = ThreadEntity(projectId = projectId, userId = userId, content = content, type = ThreadType.JOIN.name)
}
