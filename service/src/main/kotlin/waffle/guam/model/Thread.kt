package waffle.guam.model

import waffle.guam.db.entity.ImageEntity
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
    val threadImages: List<Image>,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun of(
            e: ThreadView,
            countComments: (Long) -> Long,
            filterImages: (List<ImageEntity>) -> List<Image>
        ): ThreadOverView =
            ThreadOverView(
                id = e.id,
                content = e.content,
                isEdited = e.createdAt != e.modifiedAt,
                creatorId = e.user.id,
                creatorNickname = e.user.nickname,
                creatorImageUrl = e.user.image?.path,
                commentSize = countComments.invoke(e.id),
                threadImages = filterImages.invoke(e.images),
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
    val threadImages: List<Image>,
    val comments: List<Comment>,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
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
                creatorImageUrl = e.user.image?.path,
                threadImages = filterThreadImages.invoke(e.images),
                comments = e.comments.map { Comment.of(it, filterCommentImages.invoke(it.images),) },
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}
