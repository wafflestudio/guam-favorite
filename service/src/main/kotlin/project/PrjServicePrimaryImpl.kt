package waffle.guam.project

import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.ConflictException
import waffle.guam.NotAllowedException
import waffle.guam.project.command.CreateProject
import waffle.guam.project.command.SearchProject
import waffle.guam.project.command.UpdateProject
import waffle.guam.project.event.ProjectCompleted
import waffle.guam.project.event.ProjectCreated
import waffle.guam.project.event.ProjectDeleted
import waffle.guam.project.event.ProjectUpdated
import waffle.guam.project.model.Project
import waffle.guam.task.TaskService
import waffle.guam.task.model.UserState
import waffle.guam.task.query.SearchTask.Companion.taskQuery

@Primary
@Service
class PrjServicePrimaryImpl(
    private val taskService: TaskService,
    private val prjService: ProjectService,
) : ProjectService {

    // TODO ERROR 세분화
    override fun getProject(projectId: Long): Project {

        return prjService.getProject(projectId)
    }

    override fun getAllProjects(pageable: Pageable): Page<Project> {

        return prjService.getAllProjects(pageable)
    }

    override fun getTabProjects(pageable: Pageable): Page<Project> {

        return prjService.getTabProjects(pageable)
    }

    override fun getSearchResults(pageable: Pageable, command: SearchProject): Page<Project> {

        return prjService.getSearchResults(pageable, command)
    }

    @Transactional
    override fun createProject(command: CreateProject, userId: Long): ProjectCreated {
        /**
         * Leader, Member, Guest인 프로젝트가 3개 이상일 경우, 프로젝트 참여 불가능
         */
        val checkUserTasks = taskService.getTasks(command = taskQuery().userIds(userId))

        // TODO : 한번 반려되었어도 다시 요청을 보낼 수 있도록 수정
        if (checkUserTasks.size >= 3)
            throw ConflictException("3개 이상의 프로젝트에는 참여할 수 없습니다.")

        return prjService.createProject(command, userId)
    }

    @Transactional
    override fun updateProject(command: UpdateProject, projectId: Long, userId: Long): ProjectUpdated {

        val checkPrjTasks =
            taskService.getTasks(taskQuery().projectIds(projectId).userStates(UserState.LEADER, UserState.MEMBER))

        if (checkPrjTasks.none { it.user.id == userId && it.userState == UserState.LEADER })
            throw NotAllowedException("리더만 프로젝트를 수정할 수 있어요.")

        return prjService.updateProject(command, projectId, userId)
    }

    @Transactional
    override fun deleteProject(projectId: Long, userId: Long): ProjectDeleted {

        val checkPrjTasks = taskService.getTasks(taskQuery().projectIds(projectId))

        if (checkPrjTasks.none { it.user.id == userId && it.userState == UserState.LEADER })
            throw NotAllowedException("리더만 프로젝트를 종료할 수 있어요.")

        return prjService.deleteProject(projectId, userId)
    }

    @Transactional
    override fun completeProject(projectId: Long, userId: Long): ProjectCompleted {

        val checkPrjTasks = taskService.getTasks(taskQuery().projectIds(projectId))

        if (checkPrjTasks.none { it.user.id == userId && it.userState == UserState.LEADER })
            throw NotAllowedException("리더만 프로젝트를 완료할 수 있어요.")

        return prjService.completeProject(projectId, userId)
    }
}
