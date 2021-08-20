package waffle.guam.task

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.DataNotFoundException
import waffle.guam.project.ProjectEntity
import waffle.guam.project.ProjectRepository
import waffle.guam.task.command.AcceptTask
import waffle.guam.task.command.CancelTask
import waffle.guam.task.command.CompleteTask
import waffle.guam.task.command.CreateTask
import waffle.guam.task.command.DeclineTask
import waffle.guam.task.command.JoinTask
import waffle.guam.task.command.LeaveTask
import waffle.guam.task.command.TaskCommand
import waffle.guam.task.event.TaskAccepted
import waffle.guam.task.event.TaskCanceled
import waffle.guam.task.event.TaskCompleted
import waffle.guam.task.event.TaskCreated
import waffle.guam.task.event.TaskDeclined
import waffle.guam.task.event.TaskEvent
import waffle.guam.task.event.TaskJoined
import waffle.guam.task.event.TaskLeft
import waffle.guam.task.model.Position
import waffle.guam.task.model.Task
import waffle.guam.task.model.Task.Companion.toDomain
import waffle.guam.task.model.UserState
import waffle.guam.task.query.SearchTask
import waffle.guam.task.query.SearchTask.Companion.taskQuery
import waffle.guam.task.query.TaskExtraFieldParams
import waffle.guam.user.UserRepository

@Service
class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
) : TaskService {

    @Transactional
    override fun handle(command: TaskCommand): TaskEvent =
        when (command) {
            is CreateTask -> create(command)
            is JoinTask -> join(command)
            is AcceptTask -> accept(command)
            is DeclineTask -> decline(command)
            is LeaveTask -> leave(command)
            is CancelTask -> cancel(command)
            is CompleteTask -> complete(command)
            else -> throw Exception()
        }

    private fun create(command: CreateTask): TaskCreated = TaskHandler.create(
        command = command,
        getPositionHeadCnt = { projectId, position ->
            projectRepository.findById(projectId).get().headCnt(position)
        },
        countPositionOfficialTask = { projectId, position ->
            taskRepository.findAll(
                taskQuery().projectIds(projectId).userStates(UserState.officialStates).positions(position).spec
            ).size
        },
        countUserValidTask = {
            taskRepository.findAll(taskQuery().userIds(it).userStates(UserState.validStates).spec).size
        },
        getUserOffset = { userId ->
            taskRepository.findAll(
                taskQuery().userIds(userId).spec
            ).maxByOrNull { it.userOffset }?.userOffset ?: 0
        },
        insertIntoDb = { userId, projectId, position, userState, userOffset, positionOffset ->
            taskRepository.save(
                TaskEntity(
                    project = projectRepository.findById(projectId).get(),
                    position = position.name,
                    user = userRepository.findById(userId).get(),
                    userState = userState.name,
                    userOffset = userOffset,
                    positionOffset = positionOffset
                )
            ).id
        }
    )

    private fun join(command: JoinTask): TaskJoined = TaskHandler.join(
        command = command,
        getPositionHeadCnt = { projectId, position ->
            projectRepository.findById(projectId).get().headCnt(position)
        },
        countPositionOfficialTask = { projectId, position ->
            taskRepository.findAll(
                taskQuery().projectIds(projectId).userStates(UserState.officialStates).positions(position).spec
            ).size
        },
        countUserValidTask = {
            taskRepository.findAll(taskQuery().userIds(it).userStates(UserState.validStates).spec).size
        },
        getTargetIdAndUserState = { userId, projectId ->
            taskRepository.findByUserIdAndProjectId(userId, projectId)
                ?.let { Pair(it.id, UserState.valueOf(it.userState)) }
        },
        getUserOffset = { userId ->
            taskRepository.findAll(
                taskQuery().userIds(userId).spec
            ).maxByOrNull { it.userOffset }?.userOffset ?: 0
        },
        insertIntoDb = { userId, projectId, position, userState, userOffset ->
            taskRepository.save(
                TaskEntity(
                    project = projectRepository.findById(projectId).get(),
                    position = position.name,
                    user = userRepository.findById(userId).get(),
                    userState = userState.name,
                    userOffset = userOffset
                )
            ).id
        },
    )

    private fun accept(command: AcceptTask): TaskAccepted = TaskHandler.accept(
        command = command,
        isValidLeaderId = { leaderId, projectId ->
            taskRepository.findByUserIdAndProjectId(leaderId, projectId)?.userState == UserState.LEADER.name
        },
        getTargetIdAndPosition = { userId, projectId ->
            taskRepository.findByUserIdAndProjectId(userId, projectId)
                ?.let { Triple(it.id, UserState.valueOf(it.userState), Position.valueOf(it.position)) }
        },
        getPositionHeadCnt = { projectId, position ->
            projectRepository.findById(projectId).get().headCnt(position)
        },
        countPositionOfficialTask = { projectId, position ->
            taskRepository.findAll(
                taskQuery().projectIds(projectId).userStates(UserState.officialStates).positions(position).spec
            ).size
        },
        getPositionOffset = { projectId, position ->
            taskRepository.findAll(
                taskQuery().projectIds(projectId).positions(position).spec
            ).maxByOrNull { it.positionOffset!! }?.positionOffset ?: 0
        },
        updateInDb = { taskId, userState, positionOffset ->
            taskRepository.findById(taskId).get().let {
                it.userState = userState.name
                it.positionOffset = positionOffset
            }
        },
    )

    private fun decline(command: DeclineTask): TaskDeclined = TaskHandler.decline(
        command = command,
        isValidLeaderId = { leaderId, projectId ->
            taskRepository.findByUserIdAndProjectId(leaderId, projectId)?.userState == UserState.LEADER.name
        },
        getTargetIdAndUserState = { guestId, projectId ->
            taskRepository.findByUserIdAndProjectId(guestId, projectId)
                ?.let { Pair(it.id, UserState.valueOf(it.userState)) }
        },
        updateInDb = { taskId, userState ->
            taskRepository.findById(taskId).get().let {
                it.userState = userState.name
            }
        }
    )

    private fun leave(command: LeaveTask): TaskLeft = TaskHandler.leave(
        command = command,
        getTargetIdAndUserState = { userId, projectId ->
            taskRepository.findByUserIdAndProjectId(userId, projectId)
                ?.let { Pair(it.id, UserState.valueOf(it.userState)) }
        },
        deleteFromDb = { taskId ->
            taskRepository.deleteById(taskId)
        },
        updateInDb = { taskId, userState ->
            taskRepository.findById(taskId).get().let {
                it.userState = userState.name
            }
        }
    )

    private fun cancel(command: CancelTask): TaskCanceled = TaskHandler.cancel(
        command = command,
        getTaskIdsAndUserState = { projectId ->
            taskRepository.findAllByProjectId(projectId).map { Pair(it.id, UserState.valueOf(it.userState)) }
        },
        deleteFromDb = { taskIds ->
            taskRepository.deleteByIdIn(taskIds)
        },
        updateInDb = { taskIds, userState ->
            taskRepository.updateAllByIdIn(taskIds, userState.name)
        }
    )

    private fun complete(command: CompleteTask): TaskCompleted = TaskHandler.complete(
        command = command,
        getTaskIdsAndUserState = { projectId ->
            taskRepository.findAllByProjectId(projectId).map { Pair(it.id, UserState.valueOf(it.userState)) }
        },
        deleteFromDb = { taskIds ->
            taskRepository.deleteByIdIn(taskIds)
        },
        updateInDb = { taskIds, userState ->
            taskRepository.updateAllByIdIn(taskIds, userState.name)
        }
    )

    override fun getTasks(command: SearchTask, extraFieldParams: TaskExtraFieldParams): List<Task> {
        val spec = command.specWithFetch(extraFieldParams)

        return taskRepository.findAll(spec).map { it.toDomain(extraFieldParams) }
    }

    override fun getTask(command: SearchTask, extraFieldParams: TaskExtraFieldParams): Task =
        getTasks(command, extraFieldParams).firstOrNull() ?: throw DataNotFoundException()

    private fun ProjectEntity.headCnt(position: Position): Int =
        when (position) {
            Position.FRONTEND -> frontHeadcount
            Position.BACKEND -> backHeadcount
            Position.DESIGNER -> designerHeadcount
            else -> error("")
        }
}
