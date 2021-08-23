package waffle.guam.api.request

import waffle.guam.project.command.JoinProject
import waffle.guam.task.model.Position

data class JoinProjectRequest(
    val position: Position,
    val introduction: String
) {
    fun toCommand(): JoinProject =
        JoinProject(
            position = position,
            introduction = introduction
        )
}
