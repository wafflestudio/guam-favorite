package waffle.guam.thread.command

import waffle.guam.thread.model.ThreadType

class EditThreadType(
    val threadId: Long,
    val type: ThreadType
) : ThreadCommand {
    fun toNormal(threadId: Long) = EditThreadType(
        threadId = threadId,
        type = ThreadType.NORMAL
    )
}
