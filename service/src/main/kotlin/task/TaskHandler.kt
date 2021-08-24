package waffle.guam.task

import org.springframework.stereotype.Service
import waffle.guam.project.ProjectRepository
import waffle.guam.project.model.ProjectState
import waffle.guam.task.command.AcceptTask
import waffle.guam.task.command.ApplyTask
import waffle.guam.task.command.CancelApplyTask
import waffle.guam.task.command.CancelTask
import waffle.guam.task.command.CompleteTask
import waffle.guam.task.command.CreateProjectTasks
import waffle.guam.task.command.DeclineTask
import waffle.guam.task.command.IncOrDecProjectTasks
import waffle.guam.task.command.LeaveTask
import waffle.guam.task.command.TaskCommand
import waffle.guam.task.event.TaskAccepted
import waffle.guam.task.event.TaskApplied
import waffle.guam.task.event.TaskApplyCanceled
import waffle.guam.task.event.TaskCanceled
import waffle.guam.task.event.TaskCompleted
import waffle.guam.task.event.TaskCreated
import waffle.guam.task.event.TaskDeclined
import waffle.guam.task.event.TaskEvent
import waffle.guam.task.event.TaskLeft
import waffle.guam.task.model.UserState
import waffle.guam.user.UserRepository

@Service
class TaskHandler(
    private val taskRepository: TaskRepository,
    private val taskCandidateRepository: TaskCandidateRepository,
    private val taskHistoryRepository: TaskHistoryRepository,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
) {
    fun handle(command: TaskCommand): TaskEvent =
        when (command) {
            is CreateProjectTasks -> createProjectTasks(command)
            is IncOrDecProjectTasks -> incOrDecProjectTasks(command)
            is ApplyTask -> apply(command)
            is CancelApplyTask -> cancelApply(command)
            is AcceptTask -> accept(command)
            is DeclineTask -> decline(command)
            is LeaveTask -> leave(command)
            is CancelTask -> cancel(command)
            is CompleteTask -> complete(command)
            else -> throw Exception()
        }

    private fun createProjectTasks(command: CreateProjectTasks): TaskCreated {
        val (projectId, leaderId, leaderPosition, quotas) = command

        val project = projectRepository.findById(projectId).orElseThrow {
            throw RuntimeException("해당 프로젝트를 찾을 수 없습니다.")
        }

        verifyUserQuota(leaderId)

        taskRepository.saveAll(
            quotas.map { it.toEntities(project) }.flatten()
        )

        val taskToAssign = getUnClaimedTaskOrNull(projectId, leaderPosition.name)
            ?: throw RuntimeException("해당 포지션에 더 이상 자리가 없습니다.")

        taskToAssign.user = userRepository.findById(leaderId).get()
        taskToAssign.userState = UserState.LEADER.name

        return TaskCreated(projectId = projectId)
    }

    private fun incOrDecProjectTasks(command: IncOrDecProjectTasks): TaskEvent {
        TODO()
    }

    private fun apply(command: ApplyTask): TaskApplied {
        val (userId, projectId, position) = command

        verifyUserQuota(userId)

        verifyProjectState(projectId)

        taskCandidateRepository.findByProjectIdAndUserId(projectId = projectId, userId = userId)?.let {
            throw RuntimeException("이미 지원한 상태입니다.")
        }

        taskRepository.findByProjectIdAndUserId(projectId = projectId, userId = userId)?.let {
            throw RuntimeException("이미 멤버입니다.")
        }

        getUnClaimedTaskOrNull(projectId = projectId, position = position.name)
            ?: throw RuntimeException("해당 포지션에 더 이상 자리가 없습니다.")

        taskCandidateRepository.save(
            TaskCandidateEntity(
                project = projectRepository.findById(projectId).get(),
                user = userRepository.findById(userId).get(),
                position = position.name
            )
        )

        return TaskApplied(projectId = projectId, userId = userId)
    }

    private fun cancelApply(command: CancelApplyTask): TaskApplyCanceled {
        val (projectId, userId) = command

        val candidate = taskCandidateRepository.findByProjectIdAndUserId(projectId = projectId, userId = userId)
            ?: throw RuntimeException("지원한 프로젝트가 아닙니다.")

        taskCandidateRepository.delete(candidate)

        return TaskApplyCanceled(projectId = projectId, userId = userId)
    }

    private fun accept(command: AcceptTask): TaskAccepted {
        val (leaderId, guestId, projectId) = command

        verifyProjectLeader(projectId = projectId, leaderId = leaderId)

        verifyProjectState(projectId)

        val candidate = taskCandidateRepository.findByProjectIdAndUserId(projectId = projectId, userId = guestId)
            ?: throw RuntimeException("지원한 적이 없는 사용자입니다.")

        val taskToAssign = getUnClaimedTaskOrNull(projectId, candidate.position)
            ?: throw RuntimeException("해당 포지션에 더 이상 자리가 없습니다.")

        verifyUserQuota(guestId)

        taskCandidateRepository.delete(candidate)

        taskToAssign.user = userRepository.findById(guestId).get()
        taskToAssign.userState = UserState.MEMBER.name

        return TaskAccepted(projectId = projectId, userId = guestId)
    }

    private fun decline(command: DeclineTask): TaskDeclined {
        val (leaderId, guestId, projectId) = command

        verifyProjectLeader(projectId = projectId, leaderId = leaderId)

        taskCandidateRepository.deleteByProjectIdAndUserId(projectId = projectId, userId = guestId)

        return TaskDeclined(projectId = projectId, userId = guestId)
    }

    private fun leave(command: LeaveTask): TaskLeft {
        val targetTask = taskRepository.findByProjectIdAndUserId(projectId = command.projectId, userId = command.userId)
            ?: throw RuntimeException("해당 프로젝트에 참여하고 있지 않습니다.")

        taskHistoryRepository.save(targetTask.toHistory("QUIT"))

        targetTask.user = null
        targetTask.userState = null

        return TaskLeft(userId = command.userId, projectId = command.projectId)
    }

    private fun cancel(command: CancelTask): TaskCanceled {
        val tasks = taskRepository.findAllByProjectId(command.projectId)

        taskRepository.deleteAllInBatch(tasks)

        val activeTasks = tasks.filter { it.user != null }

        taskHistoryRepository.saveAll(
            activeTasks.map { it.toHistory("CANCELED") }
        )

        return TaskCanceled(projectId = command.projectId)
    }

    private fun complete(command: CompleteTask): TaskCompleted {
        val tasks = taskRepository.findAllByProjectId(command.projectId)

        taskRepository.deleteAllInBatch(tasks)

        val activeTasks = tasks.filter { it.user != null }

        taskHistoryRepository.saveAll(
            activeTasks.map { it.toHistory("COMPLETED") }
        )

        return TaskCompleted(projectId = command.projectId)
    }

    private fun verifyProjectState(projectId: Long) {
        val project = projectRepository.findById(projectId)
            .orElseThrow { throw RuntimeException("해당 프로젝트를 찾을 수 없습니다.") }

        if (project.state != ProjectState.RECRUITING.name) {
            throw RuntimeException("해당 프로젝트의 인원 모집이 마감되었습니다.")
        }
    }

    private fun verifyProjectLeader(projectId: Long, leaderId: Long) {
        val leader = taskRepository.findByProjectIdAndUserId(projectId = projectId, userId = leaderId)
            ?: throw RuntimeException("해당 유저를 찾을 수 없습니다.")

        if (leader.userState!! != UserState.LEADER.name) {
            throw RuntimeException("리더만 해당 작업을 수행할 수 있습니다.")
        }
    }

    private fun verifyUserQuota(userId: Long) {
        if (taskRepository.findAllByUserId(userId).size >= 3) {
            throw RuntimeException("프로젝트를 더 이상 참여할 수 없습니다.")
        }
    }

    private fun getUnClaimedTaskOrNull(projectId: Long, position: String): TaskEntity? =
        taskRepository.findAllByProjectIdAndPosition(projectId = projectId, position = position)
            .firstOrNull { it.user == null }

    private fun TaskEntity.toHistory(description: String): TaskHistoryEntity =
        TaskHistoryEntity(
            project = project,
            user = user!!,
            userState = userState!!,
            position = position,
            description = description,
        )
}
