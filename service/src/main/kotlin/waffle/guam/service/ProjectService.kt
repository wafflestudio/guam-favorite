package waffle.guam.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.db.entity.Due
import waffle.guam.db.entity.ImageType
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectStackEntity
import waffle.guam.db.entity.State
import waffle.guam.db.entity.TaskEntity
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.ProjectStackRepository
import waffle.guam.db.repository.ProjectViewRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.TaskViewRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.JoinException
import waffle.guam.exception.NotAllowedException
import waffle.guam.model.Image
import waffle.guam.model.Project
import waffle.guam.model.ThreadOverView
import waffle.guam.service.command.CreateProject
import waffle.guam.service.command.CreateThread
import java.time.LocalDateTime

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val projectViewRepository: ProjectViewRepository,
    private val projectStackRepository: ProjectStackRepository,
    private val taskRepository: TaskRepository,
    private val taskViewRepository: TaskViewRepository,
    private val threadViewRepository: ThreadViewRepository,
    private val commentRepository: CommentRepository,
    private val chatService: ChatService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val searchEngine: SearchEngine = SearchEngine()

    @Transactional
    fun createProject(command: CreateProject, userId: Long): Project {
        logger.info("$command")

        if (taskRepository.countByUserIdAndStateNotLike(userId) >= 3) throw JoinException("3개 이상의 프로젝트에는 참여할 수 없습니다.")

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
                    position = myPosition, state = State.LEADER
                )
            )
            project.id
        }.let { projectId ->
            projectViewRepository.findById(projectId).orElseThrow(::DataNotFoundException)
                .let { Project.of(entity = it, fetchTasks = true) }
        }
    }

    fun getAllProjects(pageable: Pageable): Page<Project> =
        projectViewRepository.findAll(pageable).map { Project.of(it, false) }

    fun findProject(id: Long): Project =
        projectViewRepository.findById(id).orElseThrow(::DataNotFoundException)
            .let { project ->
                Project.of(
                    entity = project,
                    fetchTasks = true,
                    thread =
                    project.noticeThreadId?.let { noticeThreadId ->
                        threadViewRepository.findById(noticeThreadId).takeIf { it.isPresent }?.get()
                            ?.let { noticeThread ->
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
            .filter { it.recruiting }
            .map { Project.of(it) }

    // FIXME: 효율성 문제
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
    fun updateProject(id: Long, command: CreateProject, userId: Long) {
        logger.info("$command")

        return taskRepository.findByUserIdAndProjectId(userId, id).orElseThrow(::DataNotFoundException).let {
            if (it.state != State.LEADER) throw NotAllowedException("프로젝트 수정 권한이 없습니다.")
        }.run {
            projectStackRepository.findByProjectId(id).map {
                projectStackRepository.deleteByProjectIdAndTechStackId(id, it.techStackId)
            }

            projectStackRepository.saveAll(
                command.techStackIds.map {
                    ProjectStackEntity(projectId = id, techStackId = it.first, position = it.second)
                }
            )

            projectViewRepository.save(
                projectViewRepository.getById(id).copy(
                    title = command.title, description = command.description,
                    frontHeadcount = command.frontLeftCnt, backHeadcount = command.backLeftCnt,
                    designerHeadcount = command.designLeftCnt, modifiedAt = LocalDateTime.now()
                )
            ).let {
                Project.of(it, true)
            }
        }
    }

    @Transactional
    fun join(id: Long, userId: Long, position: Position, introduction: String): Boolean {
        logger.info("user $userId requests joining project $id as $position")

        return when {
            taskRepository.countByUserIdAndStateNotLike(userId) >= 3 -> throw JoinException("3개 이상의 프로젝트에는 참여할 수 없습니다.")
            taskRepository.findByUserIdAndProjectId(userId, id).isPresent -> throw JoinException("이미 참여하고 계신 프로젝트입니다.")
            else -> projectViewRepository.findById(id).orElseThrow(::DataNotFoundException).let {

                if (!it.recruiting)
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
                    TaskEntity(projectId = id, userId = userId, position = position, state = State.GUEST)
                )

                chatService.createThread(
                    CreateThread(id, userId, introduction, null)
                )
            }
        }
    }

    @Transactional
    fun acceptOrNot(id: Long, guestId: Long, leaderId: Long, accept: Boolean): String {
        logger.info("user $leaderId accepts guest $guestId as a member of project $id")

        return taskRepository.findByUserIdAndProjectIdAndState(leaderId, id, State.LEADER).orElseThrow(::NotAllowedException)
            .run {

                taskViewRepository.findByUserIdAndProjectId(guestId, id).orElseThrow(::DataNotFoundException).let {
                    when (it.state) {
                        State.GUEST ->

                            if (accept)
                                taskViewRepository.save(it.copy(state = State.MEMBER))
                                    .let { "정상적으로 승인되었습니다." }
                            else
                                taskViewRepository.delete(it)
                                    .let { "정상적으로 반려되었습니다." }

                        State.MEMBER -> throw NotAllowedException("이미 승인이 된 멤버입니다.")
                        State.LEADER -> throw NotAllowedException("리더를 승인할 수 없습니다.")
                    }
                }
            }
    }

    // TODO : GUEST가 자발적으로 프로젝트를 그만두는 경우도 발생할 수 있음.
    @Transactional
    fun quit(id: Long, userId: Long): Boolean {
        logger.info("Quit project $id from $userId")

        return taskRepository.findByUserIdAndProjectId(userId, id).orElseThrow(::DataNotFoundException).let {
            when (it.state) {
                State.GUEST -> throw NotAllowedException("나갈 수 없습니다. 팀의 멤버가 아닙니다")
                State.LEADER -> throw NotAllowedException("리더는 나갈 수 없습니다. 권한을 위임하거나 프로젝트를 종료해 주세요")
                State.MEMBER -> taskRepository.delete(it).let { true }
            }
        }
    }
    @Transactional
    fun deleteProject(id: Long, userId: Long): Boolean {
        logger.info("Delete project $id from $userId")

        taskRepository.findByUserIdAndProjectId(userId, id)
            .orElseThrow(::NotAllowedException).let {
                if (it.state != State.LEADER) throw NotAllowedException("프로젝트 수정 권한이 없습니다.")
            }.let {
                projectViewRepository.deleteById(id)
                return true
            }
    }
}
