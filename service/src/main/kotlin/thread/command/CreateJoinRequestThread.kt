package waffle.guam.thread.command

import waffle.guam.thread.ThreadEntity

data class CreateJoinRequestThread(
    val projectId: Long,
    val userId: Long,
    val content: String
) : ThreadCommand {
    fun toEntity() = ThreadEntity(projectId = projectId, userId = userId, content = content)
}