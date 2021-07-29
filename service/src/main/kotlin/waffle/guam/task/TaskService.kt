package waffle.guam.task

import waffle.guam.model.Task
import waffle.guam.task.command.CreateTask
import waffle.guam.task.command.SearchTask
import waffle.guam.task.event.TaskCreated

interface TaskService {
    fun createTask(userId: Long, command: CreateTask): TaskCreated
    fun getTasks(command: SearchTask): List<Task>
}
