package waffle.guam.model

import waffle.guam.db.entity.Position
import waffle.guam.db.entity.TechStackEntity

data class TechStack(
    val id: Long,
    val name: String,
    val aliases: String,
    val thumbnail: Image?,
    val position: Position
) {
    fun toEntity(): TechStackEntity =
        TechStackEntity(
            name = name,
            aliases = aliases,
            position = position
        )

    companion object {
        fun of(e: TechStackEntity): TechStack =
            TechStack(
                id = e.id,
                name = e.name,
                aliases = e.aliases,
                thumbnail =
                if (e.thumbnail != null) Image.of(e.thumbnail!!)
                else null,
                position = e.position
            )
    }
}
