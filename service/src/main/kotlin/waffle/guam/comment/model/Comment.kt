package waffle.guam.comment.model

import waffle.guam.db.entity.CommentView
import waffle.guam.model.Comment
import waffle.guam.model.Image
import java.time.LocalDateTime

data class Comment(
    val id: Long,
    val content: String?,
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
                content = e.content,
                isEdited = e.createdAt != e.modifiedAt,
                creatorId = e.user.id,
                creatorNickname = e.user.nickname,
                creatorImageUrl = e.user.image?.getPath(),
                commentImages = filteredImages,
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}
