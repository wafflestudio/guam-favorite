package waffle.guam.model

import waffle.guam.db.entity.CommentView
import java.time.LocalDateTime

data class Comment(
    val id: Long,
    val threadId: Long,
    val content: String,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun of(e: CommentView): Comment =
            Comment(
                id = e.id,
                threadId = e.threadId,
                content = e.content,
                creatorId = e.user.id,
                creatorNickname = e.user.nickname,
                creatorImageUrl = e.user.image?.path,
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}
