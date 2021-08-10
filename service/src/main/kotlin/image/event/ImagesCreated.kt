package waffle.guam.image.event

import waffle.guam.image.model.ImageType
import java.time.Instant

data class ImagesCreated(
    val imageIds: List<Long>,
    val imageType: ImageType,
    val parentId: Long,
    override val timestamp: Instant = Instant.now()
) : ImageEvent(timestamp)
