package waffle.guam.service.command

import waffle.guam.db.entity.ThreadEntity

sealed class ThreadCommand

data class CreateThread(
    val projectId: Long,
    val userId: Long,
    val content: String
) : ThreadCommand() {
    fun toEntity() = ThreadEntity(projectId = projectId, userId = userId, content = content)
}

data class EditThread(
    val threadId: Long,
    val userId: Long,
    val content: String
) : ThreadCommand()

data class DeleteThread(
    val threadId: Long,
    val userId: Long,
) : ThreadCommand()
