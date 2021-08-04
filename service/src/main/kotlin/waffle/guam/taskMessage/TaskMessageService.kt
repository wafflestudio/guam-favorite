package waffle.guam.taskMessage

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import waffle.guam.taskMessage.command.CreateTaskMessage
import waffle.guam.taskMessage.command.UpdateTaskMessage
import waffle.guam.taskMessage.event.TaskMessageCreated
import waffle.guam.taskMessage.event.TaskMessageDeleted
import waffle.guam.taskMessage.event.TaskMessageUpdated
import waffle.guam.taskMessage.model.TaskMessage

interface TaskMessageService {
    fun getTaskMessages(pageable: Pageable, taskId: Long): Page<TaskMessage>
    fun createTaskMessage(command: CreateTaskMessage): TaskMessageCreated
    fun updateTaskMessage(command: UpdateTaskMessage): TaskMessageUpdated
    fun deleteTaskMessage(messageId: Long): TaskMessageDeleted
}
