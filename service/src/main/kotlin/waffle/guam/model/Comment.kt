package waffle.guam.model

import waffle.guam.db.entity.CommentView
import waffle.guam.db.entity.ImageEntity
import java.time.LocalDateTime

data class Comment(
    val id: Long,
    val threadId: Long,
    val content: String,
    val isEdited: Boolean,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val commentImages: List<Image>,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun of(e: CommentView, filteredImages: List<Image>): Comment =
            Comment(
                id = e.id,
                threadId = e.threadId,
                content = e.content,
                isEdited = e.createdAt != e.modifiedAt,
                creatorId = e.user.id,
                creatorNickname = e.user.nickname,
                creatorImageUrl = e.user.image?.path,
                commentImages = filteredImages,
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}
