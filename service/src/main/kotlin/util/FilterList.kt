package waffle.guam.util

import waffle.guam.image.ImageEntity
import waffle.guam.image.model.Image
import waffle.guam.image.model.Image.Companion.toDomain
import waffle.guam.image.model.ImageType

object FilterList {
    fun targetImages(images: List<ImageEntity>, imageType: ImageType): List<Image> =
        images.filter { allImage -> allImage.type == imageType.name }
            .map { targetImage -> targetImage.toDomain() }
}
