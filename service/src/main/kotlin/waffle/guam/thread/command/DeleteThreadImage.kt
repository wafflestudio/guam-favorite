package waffle.guam.thread.command

import waffle.guam.db.entity.ImageType

data class DeleteThreadImage(
    val imageId: Long,
    val threadId: Long,
    val type: ImageType = ImageType.THREAD,
    val userId: Long,
) : ThreadCommand
