package waffle.guam.project_stack

import org.springframework.stereotype.Service
import waffle.guam.db.repository.ProjectStackRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.project_stack.command.StackIdList
import waffle.guam.project_stack.event.ProjectStacksCreated
import waffle.guam.project_stack.event.ProjectStacksUpdated
import waffle.guam.project_stack.model.ProjectStack

@Service
class PrjStackServiceImpl(
    private val projectStackRepository: ProjectStackRepository
) : ProjectStackService {

    override fun getProjectStacks(projectId: Long): ProjectStack {
        TODO("Not yet implemented")
    }

    override fun getAllProjectStacks(projectIds: Long): List<ProjectStack> {
        TODO("Not yet implemented")
    }

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
