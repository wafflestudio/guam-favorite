package waffle.guam.project.command

import waffle.guam.db.entity.Due
import waffle.guam.db.entity.Position

data class SearchProject(
    val query: String,
    val due: Due?,
    val stackId: Long?,
    val position: Position?
)
