package waffle.guam.image.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.image.db.ImageType

data class CreateImages(
    val files: List<MultipartFile>,
    val type: ImageType,
    val parentId: Long
) : ImageCommand
