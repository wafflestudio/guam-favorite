package waffle.guam.project

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import waffle.guam.db.entity.ImageType
import waffle.guam.db.entity.ProjectState
import waffle.guam.db.entity.UserState
import waffle.guam.db.repository.ImageRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.ProjectViewRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.project.command.CreateProject
import waffle.guam.project.command.JoinProject
import waffle.guam.project.command.SearchProject
import waffle.guam.project.command.UpdateProject
import waffle.guam.project.event.ProjectCreated
import waffle.guam.project.event.ProjectDeleted
import waffle.guam.project.event.ProjectJoinRequested
import waffle.guam.project.event.ProjectUpdated
import waffle.guam.project.model.Project
import waffle.guam.project.model.ProjectOverView
import waffle.guam.project_stack.ProjectStackService
import waffle.guam.project_stack.command.StackIdList
import waffle.guam.service.ImageInfo
import waffle.guam.service.ImageService
import waffle.guam.service.UserService
import waffle.guam.task.TaskService
import java.time.LocalDateTime

/**
 *  Entity 등 변경될 가능성이 많다. 하지만 인터페이스 구현 과정에서 고려해야 할 부분들을 직접 적용해보면서 정리하고자 한번 작성해봄
 */
@Service
class ProjectServiceImpl(
    private val projectStackService: ProjectStackService,
    private val taskService: TaskService,
    private val userService: UserService,
    private val imageService: ImageService,
    private val imageRepository: ImageRepository,
    private val projectRepository: ProjectRepository,
    private val projectViewRepository: ProjectViewRepository
) : ProjectService {

    override fun getProject(projectId: Long): ProjectOverView {
        TODO("Not yet implemented")
    }

    override fun getAllProjects(pageable: Pageable): Page<Project> {
        TODO("Not yet implemented")
    }

    override fun getTabProjects(pageable: Pageable): Page<Project> {
        TODO("Not yet implemented")
    }

    override fun getSearchResults(pageable: Pageable, command: SearchProject): Page<Project> {
        TODO("Not yet implemented")
    }

    override fun createProject(command: CreateProject, userId: Long): ProjectCreated {

        val newProject = command.toEntity()

        command.imageFiles?.let {
            newProject.thumbnail = imageService.upload(it, ImageInfo(newProject.id, ImageType.PROJECT))
        }

        return projectRepository.save(newProject).run {
            ProjectCreated(
                projectId = id,
                projectTitle = title,
                stackIdList = StackIdList(command.frontStackId, command.backStackId, command.designStackId),
                leaderId = userId,
                leaderPosition = command.myPosition!!
            )
        }
    }

    override fun updateProject(command: UpdateProject, projectId: Long, userId: Long): ProjectUpdated {

        val prj = projectViewRepository.findById(projectId).orElseThrow(::DataNotFoundException)

        prj.title = command.title ?: prj.title
        prj.description = command.description ?: prj.description
        prj.frontHeadcount = command.frontHeadCnt
        prj.backHeadcount = command.backHeadCnt
        prj.designerHeadcount = command.designHeadCnt
        prj.modifiedAt = LocalDateTime.now()
        command.imageFiles?.let { file ->
            imageRepository.deleteByParentIdAndType(prj.id, ImageType.PROJECT)
            prj.thumbnail = imageService.upload(
                file, ImageInfo(prj.id, ImageType.PROJECT)
            )
        }

        prj.techStacks = emptySet()

        return ProjectUpdated(
            projectId = prj.id,
            projectTitle = prj.title,
            stackIdList = StackIdList(command.frontStackId, command.backStackId, command.designStackId)
        )
    }

    override fun deleteProject(projectId: Long, userId: Long): ProjectDeleted {

        val prj = projectViewRepository.findById(projectId).orElseThrow(::DataNotFoundException)

        prj.run {
            state = ProjectState.CLOSED
            tasks.map {
                it.userState = UserState.QUIT
            }
        }

        return ProjectDeleted(
            projectId = prj.id,
            projectTitle = prj.title
        )
    }

    override fun joinRequestValidation(command: JoinProject, projectId: Long, userId: Long): ProjectJoinRequested {

        // 괜히 호출을 두번 하는 느낌이 있다.
        return ProjectJoinRequested(
            projectId = projectId,
            userId = userId,
            position = command.position,
            introduction = command.introduction
        )
    }
}
