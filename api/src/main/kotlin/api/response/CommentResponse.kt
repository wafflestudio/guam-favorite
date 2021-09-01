package waffle.guam.api.response

import com.fasterxml.jackson.annotation.JsonFormat
import waffle.guam.comment.model.Comment
import waffle.guam.image.model.Image
import java.time.Instant

data class CommentResponse(
    val id: Long,
    val content: String?,
    val isEdited: Boolean,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val commentImages: List<Image>,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "Asia/Seoul")
    val createdAt: Instant,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "Asia/Seoul")
    val modifiedAt: Instant,
) {
    companion object {
        fun of(d: Comment) = CommentResponse(
            id = d.id,
            content = d.content,
            isEdited = d.isEdited,
            creatorId = d.creatorId,
            creatorNickname = d.creatorNickname,
            creatorImageUrl = d.creatorImageUrl,
            commentImages = d.commentImages,
            createdAt = d.createdAt,
            modifiedAt = d.modifiedAt
        )
    }
}
