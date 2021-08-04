package waffle.guam.task

import waffle.guam.task.command.CreateTask
import waffle.guam.task.command.SearchTask
import waffle.guam.task.event.TaskCreated
import waffle.guam.task.model.Task

interface TaskService {
    fun createTask(userId: Long, command: CreateTask): TaskCreated
    fun getTasks(command: SearchTask): List<Task>
}
