package waffle.guam.projectstack

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.DataNotFoundException
import waffle.guam.projectstack.command.StackIdList
import waffle.guam.projectstack.event.ProjectStacksCreated
import waffle.guam.projectstack.event.ProjectStacksUpdated
import waffle.guam.projectstack.model.ProjectStack
import waffle.guam.stack.StackRepository
import waffle.guam.task.model.Position

@Service
class PrjStackServiceImpl(
    private val projectStackRepository: ProjectStackRepository,
    private val stackRepository: StackRepository
) : ProjectStackService {

    override fun getProjectStacks(projectId: Long): List<ProjectStack> =

        projectStackRepository.findByProjectId(projectId).map { ProjectStack.of(it) }

    override fun getAllProjectStacks(projectIds: List<Long>): List<ProjectStack> =

        projectStackRepository.findAllByProjectIds(projectIds).map { ProjectStack.of(it) }

    @Transactional
    override fun createProjectStacks(projectId: Long, command: StackIdList): ProjectStacksCreated {

        val list =
            projectStackRepository.saveAll(
                buildPrjStackList(command, projectId)
            )

        return ProjectStacksCreated(projectId, list.map { it.id })
    }

    @Transactional
    override fun updateProjectStacks(projectId: Long, command: StackIdList): ProjectStacksUpdated {

        // list 안에 존재하는 포지션의 스택만 덮어씀.
        val list = buildPrjStackList(command, projectId)
        val entities = list.map {
            projectStackRepository
                .findByProjectIdAndPosition(projectId, it.position).orElseThrow(::DataNotFoundException)
        }
        projectStackRepository.deleteAllInBatch(entities)

        projectStackRepository.saveAll(list)
        return ProjectStacksUpdated(projectId, list.map { it.id })
    }

    fun buildPrjStackList(stackIds: StackIdList, projectId: Long): List<ProjectStackEntity> {

        val res = mutableListOf<ProjectStackEntity>()
        stackIds.front?.run {
            res.add(
                ProjectStackEntity(
                    projectId = projectId,
                    position = Position.FRONTEND.name,
                    techStack = stackRepository.findById(this).orElseThrow(::DataNotFoundException)
                )
            )
        }
        stackIds.back?.run {
            res.add(
                ProjectStackEntity(
                    projectId = projectId,
                    position = Position.BACKEND.name,
                    techStack = stackRepository.findById(this).orElseThrow(::DataNotFoundException)
                )
            )
        }
        stackIds.design?.run {
            res.add(
                ProjectStackEntity(
                    projectId = projectId,
                    position = Position.DESIGNER.name,
                    techStack = stackRepository.findById(this).orElseThrow(::DataNotFoundException)
                )
            )
        }
        return res
    }
}
