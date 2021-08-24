package waffle.guam.task

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.DataNotFoundException
import waffle.guam.task.command.TaskCommand
import waffle.guam.task.event.TaskEvent
import waffle.guam.task.model.Position
import waffle.guam.task.model.Task
import waffle.guam.task.model.Task.Companion.toDomain
import waffle.guam.task.model.UserState
import waffle.guam.task.query.SearchTask
import waffle.guam.task.query.TaskExtraFieldParams
import waffle.guam.user.model.User

@Service
class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val taskCandidateRepository: TaskCandidateRepository,
    private val taskHandler: TaskHandler,
) : TaskService {

    @Transactional
    override fun handle(command: TaskCommand): TaskEvent =
        taskHandler.handle(command)

    override fun getTasks(command: SearchTask, extraFieldParams: TaskExtraFieldParams): List<Task> {
        val spec = command.specWithFetch(extraFieldParams)

        return taskRepository.findAll(spec).filter { it.user != null }.map { it.toDomain(extraFieldParams) }
    }

    override fun getTask(command: SearchTask, extraFieldParams: TaskExtraFieldParams): Task =
        getTasks(command, extraFieldParams).firstOrNull() ?: throw DataNotFoundException("해당 태스크를 찾을 수 없습니다.")

    override fun getTaskCandidates(projectId: Long): List<Task> =
        taskCandidateRepository.findAllByProjectId(projectId).map {
            Task(
                id = 444,
                position = Position.valueOf(it.position),
                projectId = it.project.id,
                user = User.of(it.user),
                userState = UserState.GUEST,
                taskMsgs = listOf(),
                createdAt = it.createdAt,
                modifiedAt = it.createdAt
            )
        }

    override fun isMemberOrLeader(projectId: Long, userId: Long): Boolean =
        taskRepository.findByProjectIdAndUserId(
            projectId = projectId,
            userId = userId
        ) != null

    override fun isMember(projectId: Long, userId: Long): Boolean =
        taskRepository.findByProjectIdAndUserId(
            projectId = projectId,
            userId = userId
        )?.userState == UserState.MEMBER.name

    override fun isLeader(projectId: Long, userId: Long): Boolean =
        taskRepository.findByProjectIdAndUserId(
            projectId = projectId,
            userId = userId
        )?.userState == UserState.LEADER.name

    override fun isGuest(projectId: Long, userId: Long): Boolean =
        taskCandidateRepository.findByProjectIdAndUserId(projectId = projectId, userId = userId) != null
}
