package waffle.guam.api.response

import waffle.guam.comment.model.Comment
import waffle.guam.image.model.Image
import waffle.guam.thread.model.ThreadDetail
import waffle.guam.thread.model.ThreadOverView
import java.time.Instant

data class ThreadOverViewResponse(
    val id: Long,
    val content: String?,
    val isEdited: Boolean,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val commentSize: Long,
    val threadImages: List<Image>,
    val createdAt: Instant,
    val modifiedAt: Instant
) {
    companion object {
        fun of(e: ThreadOverView) = ThreadOverViewResponse(
            id = e.id,
            content = e.content.ifBlank { null },
            isEdited = e.isEdited,
            creatorId = e.creatorId,
            creatorNickname = e.creatorNickname,
            creatorImageUrl = e.creatorImageUrl,
            commentSize = e.commentSize,
            threadImages = e.threadImages,
            createdAt = e.createdAt,
            modifiedAt = e.modifiedAt
        )
    }
}

data class ThreadDetailResponse(
    val id: Long,
    val content: String?,
    val isEdited: Boolean,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val threadImages: List<Image>,
    val comments: List<Comment>,
    val createdAt: Instant,
    val modifiedAt: Instant,
) {
    companion object {
        fun of(e: ThreadDetail) = ThreadDetailResponse(
            id = e.id,
            content = e.content.ifBlank { null },
            isEdited = e.isEdited,
            creatorId = e.creatorId,
            creatorNickname = e.creatorNickname,
            creatorImageUrl = e.creatorImageUrl,
            threadImages = e.threadImages,
            comments = e.comments,
            createdAt = e.createdAt,
            modifiedAt = e.modifiedAt
        )
    }
}
