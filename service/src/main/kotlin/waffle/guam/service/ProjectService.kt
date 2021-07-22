package waffle.guam.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.db.entity.Due
import waffle.guam.db.entity.ImageType
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectStackEntity
import waffle.guam.db.entity.ProjectState
import waffle.guam.db.entity.TaskEntity
import waffle.guam.db.entity.UserState
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.ProjectStackRepository
import waffle.guam.db.repository.ProjectStackViewRepository
import waffle.guam.db.repository.ProjectViewRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.TaskViewRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.db.spec.ProjectSpecs
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.JoinException
import waffle.guam.exception.NotAllowedException
import waffle.guam.model.Image
import waffle.guam.model.Project
import waffle.guam.model.ThreadOverView
import waffle.guam.service.command.CreateProject
import waffle.guam.service.command.CreateThread
import waffle.guam.service.command.UpdateProject
import java.time.LocalDateTime

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val projectViewRepository: ProjectViewRepository,
    private val projectStackRepository: ProjectStackRepository,
    private val projectStackViewRepository: ProjectStackViewRepository,
    private val taskRepository: TaskRepository,
    private val taskViewRepository: TaskViewRepository,
    private val threadViewRepository: ThreadViewRepository,
    private val commentRepository: CommentRepository,
    private val chatService: ChatService
) {

    private val searchEngine: SearchEngine = SearchEngine()

    @Transactional
    fun createProject(command: CreateProject, userId: Long): Project {

        if (taskRepository.countByUserIdAndUserStateNotLike(userId) >= 3) throw JoinException("3개 이상의 프로젝트에는 참여할 수 없습니다.")

        val myPosition = command.myPosition ?: throw JoinException("포지션을 설정해주세요.")

        return projectRepository.save(command.toEntity()).let { project ->
            projectStackRepository.saveAll(
                command.techStackIds.map { stackInfo ->
                    ProjectStackEntity(
                        projectId = project.id,
                        techStackId = stackInfo.first,
                        position = stackInfo.second
                    )
                }
            )
            taskRepository.save(
                TaskEntity(
                    projectId = project.id, userId = userId,
                    position = myPosition, userState = UserState.LEADER
                )
            )
            project.id
        }.let { projectId ->
            projectViewRepository.findById(projectId).orElseThrow(::DataNotFoundException)
                .let { Project.of(entity = it, fetchTasks = true) }
        }
    }

    fun getAllProjects(pageable: Pageable): Page<Project> =
        projectRepository.findAll(pageable)
            .map { it.id }.toList()
            .let {
                PageImpl(
                    projectViewRepository.findAll(ProjectSpecs.fetchJoinAll(it)).map { project ->
                        Project.of(project, true)
                    }
                )
            }

    fun findProject(id: Long): Project =
        projectViewRepository.findById(id).orElseThrow(::DataNotFoundException)
            .let { project ->
                Project.of(
                    entity = project,
                    fetchTasks = true,
                    thread =
                    project.noticeThreadId?.let { noticeThreadId ->
                        threadViewRepository.findById(noticeThreadId).takeIf { it.isPresent }?.get()?.let { noticeThread ->
                            ThreadOverView.of(
                                noticeThread,
                                { threadId -> commentRepository.countByThreadId(threadId) },
                                { images ->
                                    images.filter { allImage -> allImage.type == ImageType.THREAD }
                                        .map { threadImage -> Image.of(threadImage) }
                                }
                            )
                        }
                    }
                )
            }

    fun imminentProjects(): List<Project> =
        projectViewRepository
            .findByFrontHeadcountIsLessThanOrBackHeadcountIsLessThanOrDesignerHeadcountIsLessThan()
            .filter { it.state !in arrayOf(ProjectState.CLOSED, ProjectState.PENDING) }
            .map { Project.of(it) }

    // FIXME: 효율성 문제 -> 갈아엎어
    // TODO: db 상에서 남은 인원을 바로 가지고 있지 않음. 또한 stack 의 경우에도 자식 테이블을 전부 참조 해보아야한다.
    fun search(query: String, due: Due?, stackId: Long?, position: Position?): List<Project> =
        if (due == null)
            projectViewRepository.findAll()
                .map { it to searchEngine.search(dic = listOf(it.title, it.description), q = query) }
                .filter { it.second > 0 }
                .sortedBy { -it.second }
                .map { Project.of(it.first) }
        else
            projectViewRepository.findByDueEquals(due)
                .map { it to searchEngine.search(dic = listOf(it.title, it.description), q = query) }
                .filter { it.second > 0 }
                .sortedBy { -it.second }
                .map { Project.of(it.first) }

    @Transactional
    fun updateProject(projectId: Long, command: UpdateProject, userId: Long) =
        taskRepository.findByUserIdAndProjectId(userId, projectId).orElseThrow(::DataNotFoundException).let {
            if (it.userState != UserState.LEADER) throw NotAllowedException("프로젝트 수정 권한이 없습니다.")
        }.run {
            projectViewRepository.getById(projectId).let {
                it.title = command.title
                it.description = command.description
                it.frontHeadcount = command.frontHeadCnt
                it.backHeadcount = command.backHeadCnt
                it.designerHeadcount = command.designHeadCnt
                it.modifiedAt = LocalDateTime.now()
                it.techStacks =
                    projectStackRepository.saveAll(
                        command.techStackIds.map { stackInfo ->
                            ProjectStackEntity(
                                projectId = projectId,
                                techStackId = stackInfo.first,
                                position = stackInfo.second
                            )
                        }
                    ).map { projectStackEntity ->
                        projectStackViewRepository.getById(projectStackEntity.id)
                    }.toSet()
                Project.of(it, true)
            }
        }

    @Transactional
    fun join(id: Long, userId: Long, position: Position, introduction: String): Boolean =
        when {
            taskRepository.countByUserIdAndUserStateNotLike(userId) >= 3 -> throw JoinException("3개 이상의 프로젝트에는 참여할 수 없습니다.")
            taskRepository.findByUserIdAndProjectId(userId, id).isPresent -> throw JoinException("이미 참여 중인 프로젝트입니다.")
            else -> projectViewRepository.findById(id).orElseThrow(::DataNotFoundException).let {

                if (it.state != ProjectState.RECRUITING)
                    throw JoinException("이 프로젝트는 현재 팀원을 모집하고 있지 않습니다.")

                val headCnt =
                    when (position) {
                        Position.WHATEVER -> throw JoinException("포지션을 입력해주세요.")
                        Position.DESIGNER -> it!!.designerHeadcount
                        Position.BACKEND -> it!!.backHeadcount
                        Position.FRONTEND -> it!!.frontHeadcount
                    }
                val currCnt = taskRepository.countByProjectIdAndPosition(id, position)

                if (currCnt >= headCnt)
                    throw JoinException("해당 포지션에는 남은 정원이 없습니다.")

                taskRepository.save(
                    TaskEntity(projectId = id, userId = userId, position = position, userState = UserState.GUEST)
                )

                // TODO default join image 어떨까
                chatService.createThread(
                    CreateThread(id, userId, introduction, null)
                )
            }
        }

    // TODO 승인을 한번 거절하면 복구 할 수 없을까?
    @Transactional
    fun acceptOrNot(id: Long, guestId: Long, leaderId: Long, accept: Boolean): String =
        taskRepository.findByUserIdAndProjectIdAndUserState(leaderId, id, UserState.LEADER).orElseThrow(::NotAllowedException).run {
            taskViewRepository.findByUserIdAndProjectId(guestId, id).orElseThrow(::DataNotFoundException).let {
                when (it.userState) {
                    UserState.GUEST ->
                        if (accept)
                            taskViewRepository.save(it.copy(userState = UserState.MEMBER))
                                .let { "정상적으로 승인되었습니다." }
                        else
                            taskViewRepository.save(it.copy(userState = UserState.DECLINED))
                                .let { "정상적으로 반려되었습니다." }
                    UserState.MEMBER -> throw NotAllowedException("이미 승인이 된 멤버입니다.")
                    UserState.LEADER -> throw NotAllowedException("리더를 승인할 수 없습니다.")
                    UserState.QUIT -> throw NotAllowedException("이미 프로젝트를 나간 멤버입니다.")
                    UserState.DECLINED -> throw NotAllowedException("이미 승인이 거절된 멤버입니다.")
                }
            }
        }

    @Transactional
    fun quit(id: Long, userId: Long): String =
        taskRepository.findByUserIdAndProjectId(userId, id).orElseThrow(::DataNotFoundException).let {
            when (it.userState) {
                UserState.MEMBER -> taskRepository.save(it.copy(userState = UserState.QUIT)).let { "프로젝트에서 정상적으로 탈퇴되었습니다." }
                UserState.GUEST -> taskRepository.save(it.copy(userState = UserState.QUIT)).let { "가입 신청이 정상적으로 취소되었습니다." }
                UserState.LEADER -> throw NotAllowedException("리더는 나갈 수 없습니다. 권한을 위임하거나 프로젝트를 종료해 주세요")
                else -> throw NotAllowedException("이미 프로젝트에서 제외된 유저입니다.")
            }
        }

    @Transactional
    fun deleteProject(id: Long, userId: Long): String {
        taskRepository.findByUserIdAndProjectId(userId, id)
            .orElseThrow(::DataNotFoundException).let {
                if (it.userState != UserState.LEADER) throw NotAllowedException("프로젝트 삭제 권한이 없습니다.")
                projectRepository.findById(id).orElseThrow(::DataNotFoundException).let { project ->
                    project.state = ProjectState.CLOSED
                }
                return "프로젝트가 정상적으로 종료되었습니다."
            }
    }
}
