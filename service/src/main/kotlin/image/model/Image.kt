package waffle.guam.image.model

import waffle.guam.image.ImageEntity

data class Image(
    val id: Long,
    val path: String
) {
    companion object {
        fun ImageEntity.toDomain() {
            TODO()
        }
    }
}
