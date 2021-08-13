package waffle.guam.comment.model

import waffle.guam.comment.CommentEntity
import waffle.guam.image.model.Image
import waffle.guam.image.model.ImageType
import waffle.guam.image.model.ImageType.Companion.filter
import waffle.guam.user.model.User.Companion.toDomain
import java.time.Instant

data class Comment(
    val id: Long,
    val content: String?,
    val isEdited: Boolean,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val commentImages: List<Image>,
    val createdAt: Instant,
    val modifiedAt: Instant
) {

    companion object {
        fun of(e: CommentEntity): Comment =
            Comment(
                id = e.id,
                content = e.content,
                isEdited = e.createdAt != e.modifiedAt,
                creatorId = e.user.id,
                creatorNickname = e.user.nickname,
                creatorImageUrl = e.user.toDomain().imageUrl,
                commentImages = ImageType.COMMENT.filter(e.images),
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}
