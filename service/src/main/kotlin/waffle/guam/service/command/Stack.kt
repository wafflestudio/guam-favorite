package waffle.guam.service.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.TechStackEntity

sealed class StackCommand

data class CreateUpdateStack(
    val id: Long,
    val name: String,
    val aliases: String,
    val imageFiles: MultipartFile?,
    val position: Position
) : StackCommand() {
    fun toEntity(): TechStackEntity =
        TechStackEntity(
            id = id,
            name = name,
            aliases = aliases,
            position = position
        )
}
