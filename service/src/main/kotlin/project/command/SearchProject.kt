package waffle.guam.project.command

import waffle.guam.project.model.Due
import waffle.guam.task.model.Position

data class SearchProject(
    val query: String,
    val due: Due?,
    val stackId: Long?,
    val position: Position?
)
