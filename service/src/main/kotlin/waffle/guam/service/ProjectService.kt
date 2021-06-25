package waffle.guam.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectStackEntity
import waffle.guam.db.entity.TaskEntity
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.ProjectStackRepository
import waffle.guam.db.repository.ProjectViewRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.model.Project
import waffle.guam.service.command.CreateProject

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val projectViewRepository: ProjectViewRepository,
    private val projectStackRepository: ProjectStackRepository,
    private val taskRepository: TaskRepository
) {

    private val searchEngine: SearchEngine = SearchEngine()

    @Transactional
    fun createProject(command: CreateProject, userId: Long): Project =
        projectRepository.save(command.toEntity()).let { project ->
            projectStackRepository.saveAll(
                command.techStackIds.map { stackId ->
                    ProjectStackEntity(projectId = project.id, techStackId = stackId)
                }
            )
            taskRepository.save(
                TaskEntity(projectId = project.id, userId = userId, position = Position.UNKNOWN)
            )
            project.id
        }.let { projectId ->
            projectViewRepository.findById(projectId).orElseThrow(::DataNotFoundException)
                .let { Project.of(entity = it, fetchTasks = true) }
        }

    fun getAllProjects(pageable: Pageable): Page<Project> =
        projectViewRepository.findAll(pageable).map { Project.of(it) }

    fun findProject(id: Long): Project =
        projectViewRepository.findById(id).orElseThrow(::DataNotFoundException)
            .let { Project.of(entity = it, fetchTasks = true) }

    fun imminentProjects(): List<Project> =
        projectViewRepository
            .findByFrontHeadcountIsLessThanOrBackHeadcountIsLessThanOrDesignerHeadcountIsLessThan()
            .filter { it.recruiting }
            .map { Project.of(it) }

    // FIXME: 효율성 문제
    fun searchByKeyword(query: String): List<Project> =
        projectViewRepository.findAll()
            .map { it to searchEngine.search(dic = listOf(it.title, it.description), q = query) }
            .filter { it.second > 0 }
            .sortedBy { -it.second }
            .map { Project.of(it.first) }

    @Transactional
    fun updateProject() {
        TODO("업데이트는 어느 상황에 필요한가요?")
    }

    @Transactional
    fun deleteProject(id: Long): Boolean {
        projectRepository.deleteById(id)
        return true
    }
}
