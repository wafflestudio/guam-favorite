package waffle.guam.image.event

import waffle.guam.db.entity.ImageType
import java.time.Instant

data class ImagesCreated(
    val imageIds: List<Long>,
    val imageType: ImageType,
    val parentId: Long,
    override val timestamp: Instant
) : ImageEvent(timestamp)
