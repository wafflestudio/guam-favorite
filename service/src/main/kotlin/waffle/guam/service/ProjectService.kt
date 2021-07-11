package waffle.guam.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.db.entity.Due
import waffle.guam.db.entity.ImageType
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectStackEntity
import waffle.guam.db.entity.ProjectView
import waffle.guam.db.entity.State
import waffle.guam.db.entity.TaskEntity
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.ProjectStackRepository
import waffle.guam.db.repository.ProjectViewRepository
import waffle.guam.db.repository.TaskRepository
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
    private val threadViewRepository: ThreadViewRepository,
    private val commentRepository: CommentRepository,
    private val chatService: ChatService
) {

    private val searchEngine: SearchEngine = SearchEngine()

    @Transactional
    fun createProject(command: CreateProject, userId: Long): Project {

        if (taskRepository.countByUserId(userId) >= 3) throw JoinException("3개 이상의 프로젝트에는 참여할 수 없습니다.")

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
                .let { Project.of(entity = it, fetchTasks = true, currHeadCnt = currHeadCntOf(it)) }
        }
    }

    fun getAllProjects(pageable: Pageable): Page<Project> =
        projectViewRepository.findAll(pageable).map { Project.of(it, false, currHeadCnt = currHeadCntOf(it)) }

    fun findProject(id: Long): Project =
        projectViewRepository.findById(id).orElseThrow(::DataNotFoundException)
            .let {

                Project.of(
                    entity = it,
                    fetchTasks = true,
                    thread =
                    it.noticeThreadId?.let { noticeId ->
                        ThreadOverView.of(
                            threadViewRepository.getById(noticeId),
                            { threadId -> commentRepository.countByThreadId(threadId) },
                            { images ->
                                images.filter { allImage -> allImage.type == ImageType.THREAD }
                                    .map { threadImage -> Image.of(threadImage) }
                            }
                        )
                    },
                    currHeadCnt = currHeadCntOf(it)
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
    fun updateProject(id: Long, command: CreateProject, userId: Long) =

        taskRepository.findByUserIdAndProjectId(userId, id).orElseThrow(::DataNotFoundException).let {
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

    @Transactional
    fun join(id: Long, userId: Long, position: Position, introduction: String): Boolean {

        // reject when there is no quota
        // check if this pj is recruiting

        if (taskRepository.countByUserId(userId) >= 3) throw JoinException("3개 이상의 프로젝트에는 참여할 수 없습니다.")

        taskRepository.findByUserIdAndProjectId(userId, id).ifPresent {
            throw JoinException("이미 참여하고 계신 프로젝트입니다 ^~^")
        }

        return projectViewRepository.findById(id).orElseThrow(::DataNotFoundException).takeIf {
            it.recruiting
        }.let {

            val headCnt =
                when (position) {
                    Position.WHATEVER -> throw JoinException("포지션을 입력해주세요.")
                    Position.DESIGNER -> it!!.designerHeadcount
                    Position.BACKEND -> it!!.backHeadcount
                    Position.FRONTEND -> it!!.frontHeadcount
                }
            val currCnt = taskRepository.countByProjectIdAndPosition(id, position)

            if (currCnt >= headCnt) throw JoinException("해당 포지션에는 남은 정원이 없습니다.")

            taskRepository.save(
                TaskEntity(projectId = id, userId = userId, position = position, state = State.GUEST)
            )
            chatService.createThread(
                CreateThread(id, userId, introduction, null)
            )
            true
        }
    }

    @Transactional
    fun deleteProject(id: Long, userId: Long): Boolean {

        taskRepository.findByUserIdAndProjectId(userId, id).orElseThrow(::DataNotFoundException).let {
            if (it.state != State.LEADER) throw NotAllowedException("프로젝트 수정 권한이 없습니다.")
        }.let {
            projectViewRepository.deleteById(id)
            return true
        }
    }

    fun currHeadCntOf(projectView: ProjectView): IntArray {
        val res = MutableList(3, fun(_: Int) = 0)
        projectView.tasks.map {
            when (it.position) {
                Position.WHATEVER -> 0
                Position.DESIGNER -> res[2]++
                Position.BACKEND -> res[1]++
                Position.FRONTEND -> res[0]++
            }
        }
        return res.toIntArray()
    }
}
