package waffle.guam.thread.model

import waffle.guam.comment.model.Comment
import waffle.guam.image.ImageEntity
import waffle.guam.image.model.Image
import waffle.guam.image.model.Image.Companion.toDomain
import waffle.guam.thread.ThreadView
import java.time.Instant

data class ThreadDetail(
    val id: Long,
    val content: String?,
    val isEdited: Boolean,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val threadImages: List<Image>,
    val comments: List<Comment>,
    val createdAt: Instant,
    val modifiedAt: Instant
) {
    companion object {
        fun of(
            e: ThreadView,
            filterThreadImages: (List<ImageEntity>) -> List<Image>,
            filterCommentImages: (List<ImageEntity>) -> List<Image>
        ): ThreadDetail =
            ThreadDetail(
                id = e.id,
                content = e.content,
                isEdited = e.createdAt != e.modifiedAt,
                creatorId = e.user.id,
                creatorNickname = e.user.nickname,
                creatorImageUrl = e.user.image?.toDomain()?.path,
                threadImages = filterThreadImages.invoke(e.images),
                comments = e.comments.map { Comment.of(it, filterCommentImages.invoke(it.images),) },
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}
