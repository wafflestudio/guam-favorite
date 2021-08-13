package waffle.guam.stack.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.stack.StackEntity
import waffle.guam.task.model.Position

data class CreateStack(
    val name: String,
    val aliases: String,
    val imageFiles: MultipartFile?,
    val position: Position
) {
    // method toEntity occurs only when createProjects
    fun toEntity(): StackEntity =
        StackEntity(
            name = name,
            aliases = aliases,
            position = position.name
        )
}
