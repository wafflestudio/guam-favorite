package waffle.guam.model

import waffle.guam.db.entity.ThreadView
import java.time.LocalDateTime

data class ThreadOverView(
    val id: Long,
    val content: String,
    val creator: User,
    val commentSize: Long,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun of(e: ThreadView, countComments: (Long) -> Long): ThreadOverView =
            ThreadOverView(
                id = e.id,
                content = e.content,
                creator = User.of(e.user),
                commentSize = countComments.invoke(e.id),
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}

data class ThreadDetail(
    val id: Long,
    val content: String,
    val creator: User,
    val comments: List<Comment>,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun of(e: ThreadView): ThreadDetail =
            ThreadDetail(
                id = e.id,
                content = e.content,
                creator = User.of(e.user),
                comments = e.comments.map { Comment.of(it) },
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}
