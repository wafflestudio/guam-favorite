package waffle.guam.projectstack

import org.springframework.stereotype.Service
import waffle.guam.DataNotFoundException
import waffle.guam.projectstack.command.StackIdList
import waffle.guam.projectstack.event.ProjectStacksCreated
import waffle.guam.projectstack.event.ProjectStacksUpdated
import waffle.guam.projectstack.model.ProjectStack

@Service
class PrjStackServiceImpl(
    private val projectStackRepository: ProjectStackRepository
) : ProjectStackService {

    override fun getProjectStacks(projectId: Long): List<ProjectStack>

    projectStackRepository.findByProjectId(projectId).map { ProjectStack.of(it) }

    override fun getAllProjectStacks(projectIds: List<Long>): List<ProjectStack>

    projectStackRepository.findAllByProjectIds(projectIds).map { ProjectStack.of(it) }

    override fun createProjectStacks(projectId: Long, command: StackIdList): ProjectStacksCreated {

        val list =
            projectStackRepository.saveAll(command.toPrjStackList(projectId))

        return ProjectStacksCreated(projectId, list.map { it.id })
    }

    override fun updateProjectStacks(projectId: Long, command: StackIdList): ProjectStacksUpdated {

        // list 안에 존재하는 포지션의 스택만 덮어씀.
        val list = command.toPrjStackList(projectId)
        list.map {
            projectStackRepository
                .findByProjectIdAndPosition(projectId, it.position).orElseThrow(::DataNotFoundException)
                .let { target ->
                    projectStackRepository.delete(target)
                }
        }

        projectStackRepository.saveAll(list)
        return ProjectStacksUpdated(projectId, list.map { it.id })
    }
}
