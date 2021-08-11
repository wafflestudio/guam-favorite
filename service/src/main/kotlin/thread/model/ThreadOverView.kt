package waffle.guam.thread.model

import waffle.guam.image.ImageEntity
import waffle.guam.image.model.Image
import waffle.guam.thread.ThreadView
import waffle.guam.user.model.User.Companion.toDomain
import java.time.Instant

data class ThreadOverView(
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
                creatorImageUrl = e.user.toDomain().imageUrl, // e.user.image?.getPath(),
                commentSize = countComments.invoke(e.id),
                threadImages = filterImages.invoke(e.images),
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}
