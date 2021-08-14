package waffle.guam.image.model

import waffle.guam.image.ImageEntity

enum class ImageType {
    PROFILE, PROJECT, THREAD, COMMENT, STACK;

    companion object {
        fun ImageType.filter(images: List<ImageEntity>): List<ImageEntity> =
            images.filter { allImage -> allImage.type == name }
    }
}
