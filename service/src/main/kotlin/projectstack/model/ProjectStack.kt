package waffle.guam.projectstack.model

import waffle.guam.projectstack.ProjectStackEntity
import waffle.guam.task.model.Position

data class ProjectStack(
    val id: Long,
    val projectId: Long,
    val stackId: Long,
    val position: Position
) {
    companion object {
        fun of(entity: ProjectStackEntity): ProjectStack
        ProjectStack(
            id = entity.id,
            projectId = entity.projectId,
            stackId = entity.techStackId,
            position = Position.valueOf(entity.position)
        )
    }
}
