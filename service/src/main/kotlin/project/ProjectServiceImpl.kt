package waffle.guam.project

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.DataNotFoundException
import waffle.guam.project.command.CreateProject
import waffle.guam.project.command.JoinProject
import waffle.guam.project.command.SearchProject
import waffle.guam.project.command.UpdateProject
import waffle.guam.project.event.ProjectCompleted
import waffle.guam.project.event.ProjectCreated
import waffle.guam.project.event.ProjectDeleted
import waffle.guam.project.event.ProjectJoinRequested
import waffle.guam.project.event.ProjectUpdated
import waffle.guam.project.model.Project
import waffle.guam.project.model.ProjectState
import waffle.guam.projectstack.ProjectStackService
import waffle.guam.projectstack.command.StackIdList
import waffle.guam.projectstack.util.SearchEngine
import waffle.guam.task.TaskService
import waffle.guam.task.model.Position
import waffle.guam.task.model.PositionQuota
import waffle.guam.task.query.SearchTask.Companion.taskQuery
import java.time.Instant

/**
 *  Entity 등 변경될 가능성이 많다. 하지만 인터페이스 구현 과정에서 고려해야 할 부분들을 직접 적용해보면서 정리하고자 한번 작성해봄
 */
@Service
class ProjectServiceImpl(
    private val projectStackService: ProjectStackService,
    private val taskService: TaskService,
    private val projectRepository: ProjectRepository,
) : ProjectService {

    private val searchEngine = SearchEngine()

    override fun getProject(projectId: Long): Project =

        projectRepository.findById(projectId).orElseThrow(::DataNotFoundException).let { e ->
            Project.of(
                entity = e,
                techStacks = projectStackService.getProjectStacks(projectId).map { it.stack },
                tasks = taskService.getTasks(taskQuery().projectIds(projectId))
            )
        }

    override fun getAllProjects(pageable: Pageable): Page<Project> =

        projectRepository.findAll(ProjectSpec.fetchImages(), pageable).run {
            buildProjectOf(this)
        }

    override fun getTabProjects(pageable: Pageable): Page<Project> =

        projectRepository.findAll(ProjectSpec.fetchImminent(), pageable).run {
            buildProjectOf(this)
        }

    /**
     * image 이외에는 fetch 없이 불러오므로, stackId 검색 기능은 메모리 필터링을 해야 함.
     * 일단 굉장히 느릴 것으로 보임
     */
    override fun getSearchResults(pageable: Pageable, command: SearchProject): Page<Project> =

        projectRepository.findAll(
            ProjectSpec.search(due = command.due?.name, position = command.position?.name),
            pageable
        ).run {

            val ids = this.map { it.id }.toList()

            val prjStacks = projectStackService.getAllProjectStacks(ids)
                .groupBy { it.projectId }

            val tasks = taskService.getTasks(taskQuery().projectIds(ids))
                .groupBy { it.projectId }

            val filteredProjects =
                this.filter { project ->
                    command.stackId == null ||
                        prjStacks[project.id]!!.map { it.stack.id }.contains(command.stackId)
                }
                    .map { it to searchEngine.search(dic = listOf(it.title, it.description), q = command.query) }
                    .filter { it.second > 0 }
                    .sortedBy { -it.second }
                    .map { it.first }

            return PageImpl(
                filteredProjects.map {
                    Project.of(
                        it,
                        techStacks =
                        prjStacks[it.id]!!
                            .map { prjStack -> prjStack.stack },
                        tasks =
                        tasks[it.id]!!
                    )
                }
            )
        }

    @Transactional
    override fun createProject(command: CreateProject, userId: Long): ProjectCreated {

        val newProject = command.toEntity()

        return projectRepository.save(newProject).run {
            ProjectCreated(
                projectId = id,
                projectTitle = title,
                stackIdList = StackIdList(command.frontStackId, command.backStackId, command.designStackId),
                leaderId = userId,
                leaderPosition = command.myPosition!!,
                imageFiles = command.imageFiles,
                positionQuotas = listOf(
                    PositionQuota(Position.FRONTEND, frontHeadcount),
                    PositionQuota(Position.BACKEND, backHeadcount),
                    PositionQuota(Position.DESIGNER, designerHeadcount)
                )
            )
        }
    }

    @Transactional
    override fun updateProject(command: UpdateProject, projectId: Long, userId: Long): ProjectUpdated {

        val prj = projectRepository.findById(projectId).orElseThrow(::DataNotFoundException)

        prj.title = command.title ?: prj.title
        prj.description = command.description ?: prj.description
        prj.due = (command.due ?: prj.due).toString()
        prj.frontHeadcount = command.frontHeadCnt
        prj.backHeadcount = command.backHeadCnt
        prj.designerHeadcount = command.designHeadCnt
        prj.modifiedAt = Instant.now()

        return ProjectUpdated(
            projectId = prj.id,
            projectTitle = prj.title,
            stackIdList = StackIdList(command.frontStackId, command.backStackId, command.designStackId),
            imageFiles = command.imageFiles
        )
    }

    @Transactional
    override fun deleteProject(projectId: Long, userId: Long): ProjectDeleted {

        val prj = projectRepository.findById(projectId).orElseThrow(::DataNotFoundException)

        prj.run {
            state = ProjectState.CLOSED.name
        }

        return ProjectDeleted(
            projectId = prj.id,
            projectTitle = prj.title
        )
    }

    @Transactional
    override fun completeProject(projectId: Long, userId: Long): ProjectCompleted {

        val prj = projectRepository.findById(projectId).orElseThrow(::DataNotFoundException)

        prj.run {
            state = ProjectState.COMPLETED.name
        }

        return ProjectCompleted(
            projectId = prj.id,
            projectTitle = prj.title
        )
    }

    @Transactional
    override fun joinRequestValidation(command: JoinProject, projectId: Long, userId: Long): ProjectJoinRequested {

        // 괜히 호출을 두번 하는 느낌이 있다.
        return ProjectJoinRequested(
            projectId = projectId,
            userId = userId,
            position = command.position,
            introduction = command.introduction
        )
    }

    /**
     *  fetch other tables of ProjectEntity & merge
     */
    fun buildProjectOf(page: Page<ProjectEntity>): Page<Project> {

        val ids = page.map { it.id }.toList()

        val prjStacks =
            projectStackService.getAllProjectStacks(ids)
                .groupBy { it.projectId }
                .mapValues { it.value.map { prjStack -> prjStack.stack } }

        val tasks =
            taskService.getTasks(taskQuery().projectIds(ids))
                .groupBy { it.projectId }

        return page.map {
            Project.of(
                entity = it,
                techStacks = prjStacks[it.id] ?: emptyList(),
                tasks = tasks[it.id] ?: emptyList()
            )
        }
    }
}
