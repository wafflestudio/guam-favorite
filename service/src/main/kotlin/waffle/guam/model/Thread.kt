package waffle.guam.model

import waffle.guam.db.entity.ThreadView
import java.time.LocalDateTime

data class ThreadOverView(
    val id: Long,
    val content: String,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val commentSize: Long,
    val imageUrls: List<String>,
    val imageSize: Int,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun of(e: ThreadView, creatorImage: (Long) -> String?, countComments: (Long) -> Long, threadImages: (Long) -> List<String>): ThreadOverView =
                ThreadOverView(
                    id = e.id,
                    content = e.content,
                    creatorId = e.user.id,
                    creatorNickname = e.user.nickname,
                    creatorImageUrl = creatorImage.invoke(e.user.id),
                    commentSize = countComments.invoke(e.id),
                    imageUrls =  threadImages.invoke(e.id),
                    imageSize = threadImages.invoke(e.id).size,
                    createdAt = e.createdAt,
                    modifiedAt = e.modifiedAt
                )
            }
}

data class ThreadDetail(
    val id: Long,
    val content: String,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val imageUrls: List<String>,
    val comments: List<Comment>,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun of(e: ThreadView,
               creatorImage: (Long) -> String?,
               threadImages: (Long) -> List<String>,
               comments: List<Comment>
        ): ThreadDetail =
            ThreadDetail(
                id = e.id,
                content = e.content,
                creatorId = e.user.id,
                creatorNickname = e.user.nickname,
                creatorImageUrl = creatorImage.invoke(e.user.id),
                imageUrls =  threadImages.invoke(e.id),
                comments = comments,
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}