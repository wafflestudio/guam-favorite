package waffle.guam.model

import waffle.guam.db.entity.CommentView
import java.time.LocalDateTime

data class Comment(
    val id: Long,
    val threadId: Long,
    val content: String,
    val isEdited: Boolean,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val images: List<Image>,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun of(e: CommentView, creatorImage: (Long) -> String?, commentImages: (Long) -> List<Image>): Comment =
            Comment(
                id = e.id,
                threadId = e.threadId,
                content = e.content,
                isEdited = e.createdAt != e.modifiedAt,
                creatorId = e.user.id,
                creatorNickname = e.user.nickname,
                images =  commentImages.invoke(e.id),
                creatorImageUrl = e.user.image?.path,
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}
