package waffle.guam.stack.model

import waffle.guam.image.model.Image
import waffle.guam.stack.StackEntity
import waffle.guam.task.model.Position

data class TechStack(
    val id: Long,
    val name: String,
    val aliases: String,
    val thumbnail: Image?,
    val position: Position
) {
    fun toEntity(): StackEntity =
        StackEntity(
            name = name,
            aliases = aliases,
            position = position.name
        )

    companion object {
        fun of(e: StackEntity): TechStack =
            TechStack(
                id = e.id,
                name = e.name,
                aliases = e.aliases,
                thumbnail = TODO("e.thumbnail?.let { Image.stackThumbnailOf(it) }"),
                position = Position.valueOf(e.position)
            )
    }
}
