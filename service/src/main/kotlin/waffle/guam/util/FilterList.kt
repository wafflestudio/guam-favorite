package waffle.guam.util

import waffle.guam.db.entity.ImageEntity
import waffle.guam.db.entity.ImageType
import waffle.guam.model.Image

object FilterList {
    fun targetImages(images: List<ImageEntity>, imageType: ImageType): List<Image> =
        images.filter { allImage -> allImage.type == imageType }
            .map { targetImage -> Image.of(targetImage) }
}
