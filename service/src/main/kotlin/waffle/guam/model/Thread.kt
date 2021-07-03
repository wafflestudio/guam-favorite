package waffle.guam.model

import waffle.guam.db.entity.ThreadView
import java.time.LocalDateTime

data class ThreadOverView(
    val id: Long,
    val content: String,
    val isEdited: Boolean,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val commentSize: Long,
    val images: List<Image>,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun of(e: ThreadView, creatorImage: (Long) -> String?, countComments: (Long) -> Long, threadImages: (Long) -> List<Image>): ThreadOverView =
                ThreadOverView(
                    id = e.id,
                    content = e.content,
                    isEdited = e.createdAt != e.modifiedAt,
                    creatorId = e.user.id,
                    creatorNickname = e.user.nickname,
                    creatorImageUrl = creatorImage.invoke(e.user.id),
                    commentSize = countComments.invoke(e.id),
                    images =  threadImages.invoke(e.id),
                    createdAt = e.createdAt,
                    modifiedAt = e.modifiedAt
                )
            }
}

data class ThreadDetail(
    val id: Long,
    val content: String,
    val isEdited: Boolean,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val images: List<Image>,
    val comments: List<Comment>,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun of(e: ThreadView, creatorImage: (Long) -> String?, threadImages: (Long) -> List<Image>, comments: List<Comment>): ThreadDetail =
            ThreadDetail(
                id = e.id,
                content = e.content,
                isEdited = e.createdAt != e.modifiedAt,
                creatorId = e.user.id,
                creatorNickname = e.user.nickname,
                creatorImageUrl = creatorImage.invoke(e.user.id),
                images =  threadImages.invoke(e.id),
                comments = comments,
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}