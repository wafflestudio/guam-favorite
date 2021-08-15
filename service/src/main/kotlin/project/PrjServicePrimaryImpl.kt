package waffle.guam.project

import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.ConflictException
import waffle.guam.InvalidRequestException
import waffle.guam.JoinException
import waffle.guam.NotAllowedException
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
import waffle.guam.task.TaskService
import waffle.guam.task.command.SearchTask.Companion.taskQuery
import waffle.guam.task.model.Position
import waffle.guam.task.model.UserState

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

        val checkUserTasks = taskService.getTasks(command = taskQuery().userIds(userId))

        // TODO : 한번 반려되었어도 다시 요청을 보낼 수 있도록 수정
        if (checkUserTasks.size >= 3)
            throw ConflictException("3개 이상의 프로젝트에는 참여할 수 없습니다.")

        if (command.myPosition == null || command.myPosition == Position.WHATEVER)
            throw InvalidRequestException("포지션을 입력해주세요")

        return prjService.createProject(command, userId)
    }

    @Transactional
    override fun updateProject(command: UpdateProject, projectId: Long, userId: Long): ProjectUpdated {

        val checkPrjTasks =
            taskService.getTasks(taskQuery().projectIds(projectId).userStates(UserState.LEADER, UserState.MEMBER))

        if (checkPrjTasks.none { it.user.id == userId && it.userState == UserState.LEADER })
            throw NotAllowedException("리더만 프로젝트를 수정할 수 있어요.")

        when {
            checkPrjTasks.filter { it.position == Position.BACKEND }.size > command.backHeadCnt
            -> throw ConflictException("백엔드 팀원 수가 줄이려는 정원 수보다 많아요.")
            checkPrjTasks.filter { it.position == Position.FRONTEND }.size > command.frontHeadCnt
            -> throw ConflictException("프론트엔드 팀원 수가 줄이려는 정원 수보다 많아요.")
            checkPrjTasks.filter { it.position == Position.DESIGNER }.size > command.designHeadCnt
            -> throw ConflictException("디자이너 팀원 수가 줄이려는 정원 수보다 많아요.")
        }

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

    @Transactional
    override fun joinRequestValidation(command: JoinProject, projectId: Long, userId: Long): ProjectJoinRequested {

        val prj = prjService.getProject(projectId)

        if (prj.state != ProjectState.RECRUITING)
            throw JoinException("이 프로젝트는 현재 팀원을 모집하고 있지 않아요.")

        val headCnt =
            when (command.position) {
                Position.FRONTEND -> prj.frontHeadCnt
                Position.BACKEND -> prj.backHeadCnt
                Position.DESIGNER -> prj.designHeadCnt
                Position.WHATEVER -> throw JoinException("포지션을 입력해주세요.")
            }

        // TODO tasks fetch 잘 해와야 함
        val currCnt = prj.tasks!!
            .filter {
                it.userState == UserState.MEMBER || it.userState == UserState.LEADER
            }.filter {
                it.position == command.position
            }.size

        if (currCnt >= headCnt)
            throw ConflictException("해당 포지션에는 남은 정원이 없어요.")

        return prjService.joinRequestValidation(command, projectId, userId)
    }
}
