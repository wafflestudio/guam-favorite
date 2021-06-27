package waffle.guam.model

import waffle.guam.db.entity.ThreadView
import java.time.LocalDateTime

data class ThreadOverView(
    val id: Long,
    val content: String,
    // val creator: User,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val commentSize: Long,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun of(e: ThreadView, countComments: (Long) -> Long): ThreadOverView =
            User.of(e.user).let { user ->
                ThreadOverView(
                    id = e.id,
                    content = e.content,
                    creatorId = user.id,
                    creatorNickname = user.nickname,
                    creatorImageUrl = user.imageUrl,
                    commentSize = countComments.invoke(e.id),
                    createdAt = e.createdAt,
                    modifiedAt = e.modifiedAt
                )
            }
    }
}

data class ThreadDetail(
    val id: Long,
    val content: String,
    // val creator: User,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val comments: List<Comment>,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun of(e: ThreadView): ThreadDetail = User.of(e.user).let { user ->
            ThreadDetail(
                id = e.id,
                content = e.content,
                creatorId = user.id,
                creatorNickname = user.nickname,
                creatorImageUrl = user.imageUrl,
                comments = e.comments.map { Comment.of(it) },
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
        }
    }
}
