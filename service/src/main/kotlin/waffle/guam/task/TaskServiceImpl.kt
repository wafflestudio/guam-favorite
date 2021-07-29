package waffle.guam.task

import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.TaskViewRepository
import waffle.guam.model.Task
import waffle.guam.task.command.CreateTask
import waffle.guam.task.command.SearchTask
import waffle.guam.task.event.TaskCreated

class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val taskViewRepository: TaskViewRepository
) : TaskService {
    override fun createTask(userId: Long, command: CreateTask): TaskCreated =
        TaskCreated(taskRepository.save(command.toEntity(userId)).id)

    override fun getTasks(command: SearchTask): List<Task> =
        taskViewRepository.findAll(command.toSpec()).map { Task.of(it) }
}
