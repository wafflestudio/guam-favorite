package waffle.guam.thread.model

import waffle.guam.image.model.Image
import java.time.LocalDateTime

data class ThreadDetail(
    val id: Long,
    val content: String?,
    val isEdited: Boolean,
    val creatorId: Long,
    val creatorNickname: String,
    val creatorImageUrl: String?,
    val threadImages: List<Image>,
//    val comments: List<Comment>,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
//    companion object {
//        fun of(
//            e: ThreadView,
//            filterThreadImages: (List<ImageEntity>) -> List<Image>,
//            filterCommentImages: (List<ImageEntity>) -> List<Image>
//        ): ThreadDetail =
//            ThreadDetail(
//                id = e.id,
//                content = e.content,
//                isEdited = e.createdAt != e.modifiedAt,
//                creatorId = e.user.id,
//                creatorNickname = e.user.nickname,
//                creatorImageUrl = e.user.image?.getPath(),
//                threadImages = filterThreadImages.invoke(e.images),
//                comments = e.comments.map { Comment.of(it, filterCommentImages.invoke(it.images),) },
//                createdAt = e.createdAt,
//                modifiedAt = e.modifiedAt
//            )
//    }
}
