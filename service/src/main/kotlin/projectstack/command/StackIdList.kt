package waffle.guam.projectstack.command

import waffle.guam.projectstack.ProjectStackEntity
import waffle.guam.task.model.Position

class StackIdList(
    val front: Long?,
    val back: Long?,
    val design: Long?
) {
    fun toPrjStackList(projectId: Long): List<ProjectStackEntity> {
        val res = mutableListOf<ProjectStackEntity>()
        front?.run {
            res.add(
                ProjectStackEntity(
                    projectId = projectId,
                    techStackId = this,
                    position = Position.FRONTEND.name
                )
            )
        }
        back?.run {
            res.add(
                ProjectStackEntity(
                    projectId = projectId,
                    techStackId = this,
                    position = Position.BACKEND.name
                )
            )
        }
        design?.run {
            res.add(
                ProjectStackEntity(
                    projectId = projectId,
                    techStackId = this,
                    position = Position.DESIGNER.name
                )
            )
        }
        return res
    }
}
