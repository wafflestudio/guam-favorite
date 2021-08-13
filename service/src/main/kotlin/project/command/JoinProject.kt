package waffle.guam.project.command

import waffle.guam.task.model.Position

data class JoinProject(
    val position: Position,
    val introduction: String
)
