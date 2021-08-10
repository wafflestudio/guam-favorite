package waffle.guam.task

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.DataNotFoundException
import waffle.guam.task.command.CreateTask
import waffle.guam.task.command.SearchTask
import waffle.guam.task.command.TaskExtraFieldParams
import waffle.guam.task.command.UpdateTaskUserState
import waffle.guam.task.event.TaskCreated
import waffle.guam.task.event.TaskUserStateUpdated
import waffle.guam.task.model.Task
import waffle.guam.task.model.Task.Companion.toDomain
import waffle.guam.user.UserRepository

@Service
class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
) : TaskService {

    @Transactional
    override fun createTask(userId: Long, command: CreateTask): TaskCreated {
        val newTask = command.toEntity { userRepository.findById(userId).orElseThrow { DataNotFoundException() } }

        return TaskCreated(taskId = taskRepository.save(newTask).id)
    }

    @Transactional
    override fun updateTaskState(command: UpdateTaskUserState): TaskUserStateUpdated {
        val (ids, userState) = command

        taskRepository.updateStates(ids, userState.name).let { updatedCnt ->
            if (updatedCnt != ids.size) {
                throw DataNotFoundException()
            }

            return TaskUserStateUpdated(
                taskIds = ids,
                newState = userState
            )
        }
    }

    override fun getTasks(command: SearchTask, extraFieldParams: TaskExtraFieldParams): List<Task> {
        val spec = command.specWithFetch(extraFieldParams)

        return taskRepository.findAll(spec).map { it.toDomain(extraFieldParams) }
    }
}
