package waffle.guam.project_stack.command

import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectStackEntity

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
                    position = Position.FRONTEND
                )
            )
        }
        back?.run {
            res.add(
                ProjectStackEntity(
                    projectId = projectId,
                    techStackId = this,
                    position = Position.FRONTEND
                )
            )
        }
        design?.run {
            res.add(
                ProjectStackEntity(
                    projectId = projectId,
                    techStackId = this,
                    position = Position.FRONTEND
                )
            )
        }
        return res
    }
}
