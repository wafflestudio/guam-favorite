package waffle.guam.thread.event

import waffle.guam.db.entity.ImageType

data class ThreadImageDeleted(
    val threadId: Long,
    val imageId: Long,
) : ThreadEvent()