package waffle.guam.thread.model

import waffle.guam.comment.model.Comment
import waffle.guam.image.model.Image
import waffle.guam.image.model.Image.Companion.toDomain
import waffle.guam.image.model.ImageType
import waffle.guam.image.model.ImageType.Companion.filter
import waffle.guam.thread.ThreadView
import java.time.Instant

data class ThreadDetail(
    val id: Long,
    val content: String,
    val isEdited: Boolean,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val threadImages: List<Image>,
    val type: ThreadType,
    val comments: List<Comment>,
    val createdAt: Instant,
    val modifiedAt: Instant,
) {
    companion object {
        fun of(
            e: ThreadView,
        ): ThreadDetail =
            ThreadDetail(
                id = e.id,
                content = e.content,
                isEdited = e.createdAt != e.modifiedAt,
                creatorId = e.user.id,
                creatorNickname = e.user.nickname,
                creatorImageUrl = e.user.image?.toDomain()?.path,
                threadImages = ImageType.THREAD.filter(e.images).map { it.toDomain() },
                type = ThreadType.valueOf(e.type),
                comments = e.comments.map { Comment.of(it) },
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}
