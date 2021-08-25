package waffle.guam.thread.command

import waffle.guam.thread.model.ThreadType

class EditJoinThreadType(
    val projectId: Long,
    val userId: Long,
    val type: ThreadType
) : ThreadCommand {
    companion object {
        fun toNormal(projectId: Long, userId: Long) = EditJoinThreadType(
            projectId = projectId,
            userId = userId,
            type = ThreadType.NORMAL
        )
    }
}
