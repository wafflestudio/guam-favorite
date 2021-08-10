package waffle.guam.image.command

data class DeleteImages(
    val imageIds: List<Long>
) : ImageCommand
