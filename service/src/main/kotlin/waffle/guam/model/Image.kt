package waffle.guam.model

import waffle.guam.db.entity.ImageEntity

data class Image(
    val id: Long,
    val path: String
) {
    companion object {
        fun of(e: ImageEntity): Image = Image(id = e.id, path = e.path)
    }
}