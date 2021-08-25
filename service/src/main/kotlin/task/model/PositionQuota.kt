package waffle.guam.task.model

import waffle.guam.project.ProjectEntity
import waffle.guam.task.TaskEntity

data class PositionQuota(
    val position: Position,
    val cnt: Int,
) {
    fun toEntities(project: ProjectEntity) =
        (1..cnt).map { TaskEntity(project = project, position = position.name) }
}
