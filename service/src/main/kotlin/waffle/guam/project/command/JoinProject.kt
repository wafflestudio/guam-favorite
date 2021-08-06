package waffle.guam.project.command

import waffle.guam.db.entity.Position

data class JoinProject(
    val position: Position,
    val introduction: String
)
