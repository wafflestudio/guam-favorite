package waffle.guam.image.command

import waffle.guam.image.model.ImageType

sealed class DeleteImages : ImageCommand {
    data class ById(
        val imageIds: List<Long>,
    ) : DeleteImages()

    data class ByParentId(
        val parentId: Long,
        val imageType: ImageType,
    ) : DeleteImages()
}
