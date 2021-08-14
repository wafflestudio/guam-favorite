package waffle.guam.taskmessage

import org.springframework.stereotype.Service
import waffle.guam.task.TaskRepository
import waffle.guam.taskmessage.command.CreateTaskMessage
import waffle.guam.taskmessage.command.DeleteTaskMessage
import waffle.guam.taskmessage.command.UpdateTaskMessage
import waffle.guam.taskmessage.event.TaskMessageCreated
import waffle.guam.taskmessage.event.TaskMessageDeleted
import waffle.guam.taskmessage.event.TaskMessageUpdated

@Service
class TaskMessageServiceImpl(
    private val taskMessageRepository: TaskMessageRepository,
    private val taskRepository: TaskRepository
) : TaskMessageService {

    override fun createTaskMessage(command: CreateTaskMessage): TaskMessageCreated {
        TODO("Not yet implemented")
    }

    override fun updateTaskMessage(command: UpdateTaskMessage): TaskMessageUpdated {
        TODO("Not yet implemented")
    }

    override fun deleteTaskMessage(command: DeleteTaskMessage): TaskMessageDeleted {
        TODO("Not yet implemented")
    }
}
