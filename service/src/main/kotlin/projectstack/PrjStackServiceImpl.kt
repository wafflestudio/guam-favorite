package waffle.guam.projectstack

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.DataNotFoundException
import waffle.guam.projectstack.command.StackIdList
import waffle.guam.projectstack.event.ProjectStacksCreated
import waffle.guam.projectstack.event.ProjectStacksUpdated
import waffle.guam.projectstack.model.ProjectStack
import waffle.guam.stack.StackRepository

@Service
class PrjStackServiceImpl(
    private val projectStackRepository: ProjectStackRepository,
    private val stackRepository: StackRepository,
) : ProjectStackService {

    override fun getProjectStacks(projectId: Long): List<ProjectStack> =
        projectStackRepository.findByProjectId(projectId).map { ProjectStack.of(it) }

    override fun getAllProjectStacks(projectIds: List<Long>): List<ProjectStack> =
        projectStackRepository.findAllByProjectIds(projectIds).map { ProjectStack.of(it) }

    @Transactional
    override fun createProjectStacks(projectId: Long, command: StackIdList): ProjectStacksCreated =
        buildPrjStackList(command, projectId)
            .let {
                projectStackRepository.saveAll(it)
            }.let {
                ProjectStacksCreated(projectId, it.map { it.id })
            }

    @Transactional
    override fun updateProjectStacks(projectId: Long, command: StackIdList): ProjectStacksUpdated =
        buildPrjStackList(command, projectId)
            .apply {
                projectStackRepository.deleteAllByProjectIdAndPositionIn(
                    projectId = projectId,
                    positions = map { it.position }
                )
            }.let {
                projectStackRepository.saveAll(it)
            }.let {
                ProjectStacksUpdated(projectId, it.map { it.id })
            }

    private fun buildPrjStackList(stackIds: StackIdList, projectId: Long): List<ProjectStackEntity> =
        stackRepository.findAllById(stackIds.validList)
            .apply { if (size != stackIds.validCount) throw DataNotFoundException() }
            .map { ProjectStackEntity(0L, it.position, projectId, it) }
}
