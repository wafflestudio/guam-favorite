package waffle.guam.api.response

import waffle.guam.image.model.Image
import waffle.guam.stack.model.TechStack
import waffle.guam.task.model.Position

class StackResponse(
    val id: Long,
    val name: String,
    val aliases: String,
    val thumbnail: Image?,
    val position: Position
) {
    companion object {
        fun of(stack: TechStack): StackResponse =
            StackResponse(
                id = stack.id,
                name = stack.name,
                aliases = stack.aliases,
                thumbnail = stack.thumbnail,
                position = stack.position
            )
    }
}
