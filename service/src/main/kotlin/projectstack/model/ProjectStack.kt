package waffle.guam.projectstack.model

import waffle.guam.projectstack.ProjectStackView
import waffle.guam.stack.model.TechStack
import waffle.guam.task.model.Position

data class ProjectStack(
    val id: Long,
    val projectId: Long,
    val position: Position,
    val stack: TechStack
) {
    companion object {
        fun of(entity: ProjectStackView): ProjectStack =
            ProjectStack(
                id = entity.id,
                projectId = entity.projectId,
                position = Position.valueOf(entity.position),
                stack = TechStack.of(entity.techStack)
            )
    }
}
