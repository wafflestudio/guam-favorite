package waffle.guam.taskMessage

import waffle.guam.taskMessage.command.CreateTaskMessage
import waffle.guam.taskMessage.command.UpdateTaskMessage
import waffle.guam.taskMessage.event.TaskMessageCreated
import waffle.guam.taskMessage.event.TaskMessageDeleted
import waffle.guam.taskMessage.event.TaskMessageUpdated

interface TaskMessageService {
    fun createTaskMessage(command: CreateTaskMessage): TaskMessageCreated
    fun updateTaskMessage(command: UpdateTaskMessage): TaskMessageUpdated
    fun deleteTaskMessage(messageId: Long): TaskMessageDeleted
}
