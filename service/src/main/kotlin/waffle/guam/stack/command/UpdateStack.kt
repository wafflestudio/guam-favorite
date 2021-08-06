package waffle.guam.stack.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.db.entity.Position

data class UpdateStack(
    val name: String?,
    val aliases: String?,
    val imageFiles: MultipartFile?,
    val position: Position?
) {
    // method toEntity occurs only when createProjects
}
