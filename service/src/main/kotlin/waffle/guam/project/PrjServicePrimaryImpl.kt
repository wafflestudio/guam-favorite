package waffle.guam.project

import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectState
import waffle.guam.db.entity.UserState
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.ProjectViewRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.JoinException
import waffle.guam.exception.NotAllowedException
import waffle.guam.model.Project
import waffle.guam.project.command.CreateProject
import waffle.guam.project.command.JoinProject
import waffle.guam.project.command.SearchProject
import waffle.guam.project.command.UpdateProject
import waffle.guam.project.event.ProjectCreated
import waffle.guam.project.event.ProjectDeleted
import waffle.guam.project.event.ProjectJoinRequested
import waffle.guam.project.event.ProjectUpdated
import waffle.guam.project.model.ProjectOverView
import waffle.guam.project_stack.ProjectStackService
import waffle.guam.service.ImageService
import waffle.guam.service.UserService
import waffle.guam.task.TaskService

/**
 *  Entity 등 변경될 가능성이 많다. 하지만 인터페이스 구현 과정에서 고려해야 할 부분들을 직접 적용해보면서 정리하고자 한번 작성해봄
 */
@Primary
@Service
class PrjServicePrimaryImpl(
    private val projectStackService: ProjectStackService,
    private val taskService: TaskService,
    private val userService: UserService,
    private val imageService: ImageService,
    private val projectRepository: ProjectRepository,
    private val projectViewRepository: ProjectViewRepository,
    private val prjService: ProjectService
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

        if (userService.get(userId).projects.size >= 3)
            throw JoinException("3개 이상의 프로젝트에는 참여할 수 없습니다.")

        if (command.myPosition == null || command.myPosition == Position.WHATEVER)
            throw JoinException("포지션을 입력해주세요")

        return prjService.createProject(command, userId)
    }

    override fun updateProject(command: UpdateProject, projectId: Long, userId: Long): ProjectUpdated {

        // TODO: userProject 객체에 리더 여부를 담아 주시면 로직을 깔끔히 가져갈 수 있을 것 같습니다.
        //  ++ 프로젝트를 전부 불러왔을 때 비로소 유저 상태를 체크할 수 있으므로, 로직을 분리하게 되면 디비를 두번 조회하여 낭비가 발생함. ㅜ
        val prj = projectViewRepository.findById(projectId).orElseThrow(::DataNotFoundException)
        if (prj.tasks.none { it.user.id == userId && it.userState == UserState.LEADER }) throw NotAllowedException("리더만 프로젝트를 수정할 수 있어요.")

        val members = prj.tasks.filter { it.userState == UserState.LEADER || it.userState == UserState.MEMBER }
        if (members.filter { it.position == Position.BACKEND }.size > command.backHeadCnt) throw NotAllowedException("백엔드 정원을 현재 팀원들요 수보다 더 줄일 수 없어요.")
        if (members.filter { it.position == Position.FRONTEND }.size > command.frontHeadCnt) throw NotAllowedException("프론트엔드 정원을 현재 팀원들 수보다 더 줄일 수 없어요.")
        if (members.filter { it.position == Position.DESIGNER }.size > command.designHeadCnt) throw NotAllowedException("디자이너 정원을 현재 팀원들 수보다 더 줄일수 없어요.")

        return prjService.updateProject(command, projectId, userId)
    }

    override fun deleteProject(projectId: Long, userId: Long): ProjectDeleted {

        // TODO: userProject 객체에 리더 여부를 담아 주시면 로직을 깔끔히 가져갈 수 있을 것 같습니다.
        //  ++ 삭제 / 종료 구분 언제 하지
        val prj = projectViewRepository.findById(projectId).orElseThrow(::DataNotFoundException)
        if (prj.tasks.none { it.user.id == userId && it.userState == UserState.LEADER }) throw NotAllowedException("리더만 프로젝트를 종료할 수 있어요.")

        return prjService.deleteProject(projectId, userId)
    }

    override fun joinRequestValidation(command: JoinProject, projectId: Long, userId: Long): ProjectJoinRequested {

        val prj = projectViewRepository.findById(projectId).orElseThrow(::DataNotFoundException)

        if (prj.state != ProjectState.RECRUITING) throw JoinException("이 프로젝트는 현재 팀원을 모집하고 있지 않아요.")

        val headCnt =
            when (command.position) {
                Position.FRONTEND -> prj.frontHeadcount
                Position.BACKEND -> prj.backHeadcount
                Position.DESIGNER -> prj.designerHeadcount
                Position.WHATEVER -> throw JoinException("포지션을 입력해주세요.")
            }

        val currCnt = prj.tasks
            .filter {
                it.userState == UserState.MEMBER || it.userState == UserState.LEADER
            }.filter {
                it.position == command.position
            }.size

        if (currCnt >= headCnt) throw JoinException("해당 포지션에는 남은 정원이 없어요.")

        return prjService.joinRequestValidation(command, projectId, userId)
    }
}
