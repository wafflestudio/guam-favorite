package waffle.guam.model

import waffle.guam.db.entity.TechStackEntity

data class TechStack(
    val name: String,
    val aliases: String,
    val thumbnail: String
) {
    fun toEntity(): TechStackEntity =
        TechStackEntity(
            name = name,
            aliases = aliases,
            thumbnail = thumbnail
        )

    companion object {
        fun of(e: TechStackEntity): TechStack =
            TechStack(
                name = e.name,
                aliases = e.aliases,
                thumbnail = e.thumbnail
            )
    }
}
