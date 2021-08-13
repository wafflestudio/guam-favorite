package waffle.guam.image.model

import waffle.guam.image.ImageEntity
import waffle.guam.image.model.Image.Companion.toDomain

enum class ImageType {
    PROFILE, PROJECT, THREAD, COMMENT, STACK;

    companion object {
        fun ImageType.filter(images: List<ImageEntity>): List<Image> =
            images.filter { allImage -> allImage.type == name }
                .map { targetImage -> targetImage.toDomain() }
    }
}
