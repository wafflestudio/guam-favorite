package waffle.guam.user

import waffle.guam.db.entity.UserState
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.JoinException
import waffle.guam.exception.NotAllowedException
import waffle.guam.task.TaskService
import waffle.guam.task.command.SearchTask

class UserActionFilterImpl(
    private val taskService: TaskService
) : UserActionFilter {
    companion object {
        const val MAXIMUM_TASK_SIZE = 3
    }

    override fun tryCreateProject(userId: Long) {
        val tasks = taskService.getTasks(
            command = SearchTask(
                userId = userId,
                userStates = listOf(UserState.GUEST, UserState.MEMBER, UserState.LEADER),
            )
        )

        if (tasks.size >= MAXIMUM_TASK_SIZE) throw JoinException("3개 이상의 프로젝트에는 참여할 수 없습니다.")
    }

    override fun tryUpdateProject(userId: Long, projectId: Long) {
        val tasks = taskService.getTasks(
            command = SearchTask(
                userId = userId,
                projectId = projectId,
                userStates = listOf(UserState.LEADER)
            )
        )

        if (tasks.isEmpty()) throw NotAllowedException("프로젝트 수정 권한이 없습니다.")
    }

    override fun tryDeleteProject(userId: Long, projectId: Long) {
        val tasks = taskService.getTasks(
            command = SearchTask(
                userId = userId,
                projectId = projectId,
                userStates = listOf(UserState.LEADER)
            )
        )

        if (tasks.isEmpty()) throw NotAllowedException("프로젝트 삭제 권한이 없습니다.")
    }

    override fun tryJoinProject(userId: Long, projectId: Long) {
        val tasks = taskService.getTasks(
            command = SearchTask(
                userId = userId,
                userStates = listOf(UserState.GUEST, UserState.MEMBER, UserState.LEADER),
            )
        )

        if (tasks.size >= MAXIMUM_TASK_SIZE) throw JoinException("3개 이상의 프로젝트에는 참여할 수 없습니다.")
        if (tasks.map { it.projectId }.contains(projectId)) throw JoinException("이미 참여 중인 프로젝트입니다.")
    }

    override fun tryQuitProject(userId: Long, projectId: Long) {
        val task = taskService.getTasks(
            command = SearchTask(
                userId = userId,
                projectId = projectId
            )
        ).firstOrNull() ?: throw DataNotFoundException("Task with userId: $userId, projectId: $projectId")

        when (task.userState) {
            UserState.LEADER -> throw NotAllowedException("리더는 나갈 수 없습니다. 권한을 위임하거나 프로젝트를 종료해 주세요")
            UserState.DECLINED, UserState.QUIT -> throw NotAllowedException("이미 프로젝트에서 제외된 유저입니다.")
        }
    }
}
