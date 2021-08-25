package waffle.guam.task

import waffle.guam.task.command.TaskCommand
import waffle.guam.task.event.TaskEvent
import waffle.guam.task.model.Task
import waffle.guam.task.query.SearchTask
import waffle.guam.task.query.TaskExtraFieldParams

interface TaskService {
    fun handle(command: TaskCommand): TaskEvent
    fun getTasks(command: SearchTask, extraFieldParams: TaskExtraFieldParams = TaskExtraFieldParams()): List<Task>
    // FIXME:: Project의 tasks에 GUEST는 빠지고, JoinThread column으로 조인쓰레드를 구분하도록 바꿔야함.
    fun getTaskCandidates(projectId: Long): List<Task>
    fun getTask(taskId: Long): Task
    fun getTask(command: SearchTask, extraFieldParams: TaskExtraFieldParams = TaskExtraFieldParams()): Task
    fun isMemberOrLeader(projectId: Long, userId: Long): Boolean
    fun isLeader(projectId: Long, userId: Long): Boolean
    fun isMember(projectId: Long, userId: Long): Boolean
    fun isGuest(projectId: Long, userId: Long): Boolean
}
