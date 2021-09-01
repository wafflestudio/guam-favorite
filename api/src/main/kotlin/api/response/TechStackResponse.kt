package waffle.guam.api.response

import waffle.guam.image.model.Image
import waffle.guam.stack.model.TechStack
import waffle.guam.task.model.Position

data class TechStackResponse(
    val id: Long,
    val name: String,
    val aliases: String,
    val thumbnail: Image?,
    val position: Position,
) {
    companion object {
        fun of(d: TechStack) = TechStackResponse(
            id = d.id,
            name = d.name,
            aliases = d.aliases,
            thumbnail = d.thumbnail,
            position = d.position
        )
    }
}
