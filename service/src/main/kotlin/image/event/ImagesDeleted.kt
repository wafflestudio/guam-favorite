package waffle.guam.image.event

import java.time.Instant

data class ImagesDeleted(
    val imageIds: List<Long>,
    override val timestamp: Instant = Instant.now()
) : ImageEvent(timestamp)
