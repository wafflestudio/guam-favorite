package waffle.guam.taskmessage

import waffle.guam.taskmessage.command.CreateTaskMessage
import waffle.guam.taskmessage.command.UpdateTaskMessage
import waffle.guam.taskmessage.event.TaskMessageCreated
import waffle.guam.taskmessage.event.TaskMessageDeleted
import waffle.guam.taskmessage.event.TaskMessageUpdated

interface TaskMessageService {
    fun createTaskMessage(command: CreateTaskMessage): TaskMessageCreated
    fun updateTaskMessage(command: UpdateTaskMessage): TaskMessageUpdated
    fun deleteTaskMessage(messageId: Long): TaskMessageDeleted
}
