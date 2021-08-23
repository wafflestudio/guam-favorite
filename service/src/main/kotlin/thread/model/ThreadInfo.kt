package waffle.guam.thread.model

import waffle.guam.thread.ThreadEntity
import java.time.Instant

data class ThreadInfo(
    val id: Long,
    val content: String?,
    val isEdited: Boolean,
    val creatorId: Long,
    val type: ThreadType,
    val createdAt: Instant,
    val modifiedAt: Instant
) {
    companion object {
        fun of(
            e: ThreadEntity
        ): ThreadInfo =
            ThreadInfo(
                id = e.id,
                content = e.content.ifBlank { null },
                isEdited = e.createdAt != e.modifiedAt,
                creatorId = e.userId,
                type = ThreadType.valueOf(e.type),
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}
