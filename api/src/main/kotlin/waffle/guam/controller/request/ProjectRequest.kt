package waffle.guam.controller.request

import waffle.guam.db.entity.Due
import waffle.guam.db.entity.Position

data class SearchProject(
    val keyword: String,
    val stackId: Long?,
    val position: Position?,
    val due: Due?
)
