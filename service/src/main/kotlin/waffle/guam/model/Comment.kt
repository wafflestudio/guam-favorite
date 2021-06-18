package waffle.guam.model

import waffle.guam.db.entity.CommentView
import java.time.LocalDateTime

data class Comment(
    val id: Long,
    val threadId: Long,
    val content: String,
    val creator: User,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun of(e: CommentView): Comment =
            Comment(
                id = e.id,
                threadId = e.threadId,
                content = e.content,
                creator = User.of(e.user),
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}
