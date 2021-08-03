package waffle.guam.thread.event

import waffle.guam.db.entity.ImageType

data class ThreadImageDeleted(
    val imageId: Long,
    val commentId: Long,
    val type: ImageType = ImageType.COMMENT,
    val userId: Long,
) : ThreadEvent()