package waffle.guam.task

import waffle.guam.task.command.CreateTask
import waffle.guam.task.command.SearchTask
import waffle.guam.task.command.TaskExtraFieldParams
import waffle.guam.task.command.UpdateTaskUserState
import waffle.guam.task.event.TaskCreated
import waffle.guam.task.event.TaskUserStateUpdated
import waffle.guam.task.model.Task

interface TaskService {
    fun createTask(userId: Long, command: CreateTask): TaskCreated
    fun updateTaskState(command: UpdateTaskUserState): TaskUserStateUpdated
    fun getTasks(command: SearchTask, extraFieldParams: TaskExtraFieldParams = TaskExtraFieldParams()): List<Task>
}
