package waffle.guam.model

import waffle.guam.db.entity.CommentView
import java.time.LocalDateTime

data class Comment(
    val id: Long,
    val threadId: Long,
    val content: String,
    // val creator: User,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun of(e: CommentView): Comment = User.of(e.user).let {
            Comment(
                id = e.id,
                threadId = e.threadId,
                content = e.content,
                creatorId = it.id,
                creatorNickname = it.nickname,
                creatorImageUrl = it.imageUrl,
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
        }
    }
}
