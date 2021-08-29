package waffle.guam.api.request

import waffle.guam.project.command.SearchProject
import waffle.guam.project.model.Due
import waffle.guam.task.model.Position

data class SearchProjectRequest(
    val keyword: String,
    val due: Due?,
    val stackId: Long?,
    val position: Position?
) {
    fun toCommand(): SearchProject =
        SearchProject(
            keyword, due, stackId, position
        )
}
