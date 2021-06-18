package waffle.guam.model

import waffle.guam.db.entity.UserEntity

data class User(
    val id: Long,
    val name: String,
    val imageUrl: String?
) {
    companion object {
        fun of(e: UserEntity): User =
            User(
                id = e.id,
                name = e.name,
                imageUrl = e.imageUrl
            )
    }
}
