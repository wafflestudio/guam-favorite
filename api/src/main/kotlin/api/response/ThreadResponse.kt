package waffle.guam.api.response

import com.fasterxml.jackson.annotation.JsonFormat
import waffle.guam.image.model.Image
import waffle.guam.thread.model.ThreadDetail
import waffle.guam.thread.model.ThreadOverView
import waffle.guam.thread.model.ThreadType
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
    val type: ThreadType,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "Asia/Seoul")
    val createdAt: Instant,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "Asia/Seoul")
    val modifiedAt: Instant,
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
            type = e.type,
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
    val type: ThreadType,
    val comments: List<CommentResponse>,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "Asia/Seoul")
    val createdAt: Instant,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "Asia/Seoul")
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
            type = e.type,
            comments = e.comments.map { CommentResponse.of(it) },
            createdAt = e.createdAt,
            modifiedAt = e.modifiedAt
        )
    }
}
