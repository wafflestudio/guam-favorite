package waffle.guam.service.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.TechStackEntity

sealed class StackCommand

data class CreateStack(
    val name: String,
    val aliases: String,
    val imageFiles: MultipartFile?,
    val position: Position
) : StackCommand() {
    // method toEntity occurs only when createProjects
    fun toEntity(): TechStackEntity =
        TechStackEntity(
            name = name,
            aliases = aliases,
            position = position
        )
}

data class UpdateStack(
    val name: String?,
    val aliases: String?,
    val imageFiles: MultipartFile?,
    val position: Position?
) : StackCommand() {
    // method toEntity occurs only when createProjects
}
