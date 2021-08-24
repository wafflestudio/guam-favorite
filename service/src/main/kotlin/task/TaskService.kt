package waffle.guam.task

import waffle.guam.task.command.TaskCommand
import waffle.guam.task.event.TaskEvent
import waffle.guam.task.model.Task
import waffle.guam.task.query.SearchTask
import waffle.guam.task.query.TaskExtraFieldParams

interface TaskService {
    fun handle(command: TaskCommand): TaskEvent
    fun getTasks(command: SearchTask, extraFieldParams: TaskExtraFieldParams = TaskExtraFieldParams()): List<Task>
    fun getTask(command: SearchTask, extraFieldParams: TaskExtraFieldParams = TaskExtraFieldParams()): Task
    fun isMemberOrLeader(projectId: Long, userId: Long): Boolean
    fun isLeader(projectId: Long, userId: Long): Boolean
    fun isMember(projectId: Long, userId: Long): Boolean
    fun isGuest(projectId: Long, userId: Long): Boolean
}
