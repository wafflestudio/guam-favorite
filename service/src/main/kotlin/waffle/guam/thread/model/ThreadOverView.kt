package waffle.guam.thread.model

import waffle.guam.db.entity.ImageEntity
import waffle.guam.db.entity.ThreadView
import waffle.guam.model.Image
import java.time.LocalDateTime

data class ThreadOverView(
    val id: Long,
    val content: String?,
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
                creatorImageUrl = e.user.image?.getPath(),
                commentSize = countComments.invoke(e.id),
                threadImages = filterImages.invoke(e.images),
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}
