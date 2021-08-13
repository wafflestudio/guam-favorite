package waffle.guam.util

import waffle.guam.image.ImageEntity
import waffle.guam.image.ImageType
import waffle.guam.image.model.Image
import waffle.guam.image.model.Image.Companion.toDomain

object FilterList {
    fun targetImages(images: List<ImageEntity>, imageType: ImageType): List<Image> =
        images.filter { allImage -> allImage.type == imageType }
            .map { targetImage -> targetImage.toDomain() }
}
