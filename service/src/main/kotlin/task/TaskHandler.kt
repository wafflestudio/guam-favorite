package waffle.guam.task

import waffle.guam.ConflictException
import waffle.guam.DataNotFoundException
import waffle.guam.task.command.AcceptTask
import waffle.guam.task.command.CancelTask
import waffle.guam.task.command.CompleteTask
import waffle.guam.task.command.CreateTask
import waffle.guam.task.command.DeclineTask
import waffle.guam.task.command.JoinTask
import waffle.guam.task.command.LeaveTask
import waffle.guam.task.event.TaskAccepted
import waffle.guam.task.event.TaskCanceled
import waffle.guam.task.event.TaskCompleted
import waffle.guam.task.event.TaskCreated
import waffle.guam.task.event.TaskDeclined
import waffle.guam.task.event.TaskJoined
import waffle.guam.task.event.TaskLeft
import waffle.guam.task.model.Position
import waffle.guam.task.model.UserState

object TaskHandler {
    fun create(
        command: CreateTask,
        getPositionHeadCnt: (projectId: Long, position: Position) -> Int,
        countPositionOfficialTask: (projectId: Long, position: Position) -> Int,
        countUserValidTask: (Long) -> Int,
        getUserOffset: (Long) -> Int,
        insertIntoDb: (userId: Long, projectId: Long, position: Position, userState: UserState, userOffset: Int, projectOffset: Int) -> Long,
    ): TaskCreated {
        val (userId, projectId, position) = command

        /**
         * Official-tasks is a set of tasks of which state is either MEMBER or LEADER.
         * The count of (official task - position) cannot exceed the headcount of the position.
         */
        val officialTaskCnt = countPositionOfficialTask(projectId, position)
        val maximumTaskCnt = getPositionHeadCnt(projectId, position)

        if (officialTaskCnt >= maximumTaskCnt) {
            throw Exception("포지션 정원 초과")
        }

        /**
         * Count of valid task per user cannot exceed 3.
         */
        val userValidTaskCnt = countUserValidTask(userId)

        if (userValidTaskCnt >= 3) {
            throw Exception("인당 태스크 개수 초과")
        }

        val currentUserOffset = getUserOffset(userId)

        val newTaskId = insertIntoDb(userId, projectId, position, UserState.LEADER, currentUserOffset + 1, 1)

        return TaskCreated(
            taskId = newTaskId
        )
    }

    fun join(
        command: JoinTask,
        getPositionHeadCnt: (projectId: Long, position: Position) -> Int,
        countPositionOfficialTask: (projectId: Long, position: Position) -> Int,
        countUserValidTask: (Long) -> Int,
        getTargetIdAndUserState: (userId: Long, projectId: Long) -> Pair<Long, UserState>?,
        getUserOffset: (Long) -> Int,
        insertIntoDb: (userId: Long, projectId: Long, position: Position, userState: UserState, userOffset: Int) -> Long,
    ): TaskJoined {
        val (userId, projectId, position) = command

        /**
         * Official-tasks is a set of tasks of which state is either MEMBER or LEADER.
         * The count of (official task - position) cannot exceed the headcount of the position.
         */
        val officialTaskCnt = countPositionOfficialTask(projectId, position)
        val maximumTaskCnt = getPositionHeadCnt(projectId, position)

        if (officialTaskCnt >= maximumTaskCnt) {
            throw Exception("포지션 정원 초과")
        }

        /**
         * Count of valid task per user cannot exceed 3.
         */
        val userValidTaskCnt = countUserValidTask(userId)

        if (userValidTaskCnt >= 3) {
            throw Exception("인당 태스크 개수 초과")
        }

        /**
         * User cannot have multiple tasks in a project
         */
        getTargetIdAndUserState(userId, projectId)?.second?.run {
            when (this) {
                UserState.GUEST, UserState.LEADER, UserState.MEMBER -> throw Exception("이미 참여 중")
                UserState.CANCELED, UserState.CONTRIBUTED, UserState.LEFT -> throw Exception("참여할 수 없는 플젝")
                UserState.DECLINED -> throw Exception("이미 거절 당함.")
            }
        }

        val currentUserOffset = getUserOffset(userId)

        val newTaskId = insertIntoDb(userId, projectId, position, UserState.GUEST, currentUserOffset + 1)

        return TaskJoined(
            taskId = newTaskId
        )
    }

    fun leave(
        command: LeaveTask,
        getTargetIdAndUserState: (userId: Long, projectId: Long) -> Pair<Long, UserState>?,
        deleteFromDb: (Long) -> Unit,
        updateInDb: (Long, UserState) -> Unit,
    ): TaskLeft {
        val (userId, projectId) = command

        val (targetId, userState) = getTargetIdAndUserState(userId, projectId) ?: throw Exception()

        /**
         * Only valid task can leave the project.
         */
        if (!userState.isValidState()) {
            throw Exception()
        }

        /**
         * If target is guest, it is removed or else turn into LEFT state.
         */
        if (userState == UserState.GUEST) {
            deleteFromDb(targetId)
        } else {
            updateInDb(targetId, UserState.LEFT)
        }

        return TaskLeft(userId = userId, projectId = projectId)
    }

    fun accept(
        command: AcceptTask,
        isValidLeaderId: (userId: Long, projectId: Long) -> Boolean,
        getTargetIdAndPosition: (userId: Long, projectId: Long) -> Triple<Long, UserState, Position>?,
        getPositionHeadCnt: (projectId: Long, position: Position) -> Int,
        countPositionOfficialTask: (projectId: Long, position: Position) -> Int,
        getPositionOffset: (projectId: Long, position: Position) -> Int,
        updateInDb: (taskId: Long, userState: UserState, positionOffset: Int) -> Unit,
    ): TaskAccepted {
        val (leaderId, guestId, projectId) = command

        /**
         * Only the leader of the project can decline guests.
         */
        if (!isValidLeaderId(leaderId, projectId)) {
            throw Exception("리더가 아님..")
        }

        val (targetId, userState, position) = getTargetIdAndPosition(guestId, projectId)
            ?: throw DataNotFoundException()

        /**
         * Only guest can be accepted.
         */
        if (userState != UserState.GUEST) {
            throw Exception("게스트가 아닌 사람을 accept")
        }

        /**
         * Official-tasks is a set of tasks of which state is either MEMBER or LEADER.
         * The count of (official task - position) cannot exceed the headcount of the position.
         */

        val officialTaskCnt = countPositionOfficialTask(projectId, position)
        val maximumTaskCnt = getPositionHeadCnt(projectId, position)

        if (officialTaskCnt >= maximumTaskCnt) {
            throw ConflictException("해당 포지션에는 남은 정원이 없어요.")
        }

        val positionOffset = getPositionOffset(projectId, position)

        updateInDb(targetId, UserState.MEMBER, positionOffset + 1)

        return TaskAccepted(taskId = targetId)
    }

    fun decline(
        command: DeclineTask,
        isValidLeaderId: (userId: Long, projectId: Long) -> Boolean,
        getTargetIdAndUserState: (userId: Long, projectId: Long) -> Pair<Long, UserState>?,
        updateInDb: (taskId: Long, userState: UserState) -> Unit,
    ): TaskDeclined {
        val (leaderId, guestId, projectId) = command

        /**
         * Only the leader of the project can decline guests.
         */
        if (!isValidLeaderId(leaderId, projectId)) {
            throw Exception("리더가 아님..")
        }

        val (targetId, userState) = getTargetIdAndUserState(guestId, projectId) ?: throw DataNotFoundException()

        /**
         * Only guest can be declined.
         */
        if (userState != UserState.GUEST) {
            throw Exception("유저 상태가 그 사이에 업데이트 된 경우")
        }

        updateInDb(targetId, UserState.DECLINED)

        return TaskDeclined(taskId = targetId)
    }

    fun cancel(
        command: CancelTask,
        getTaskIdsAndUserState: (projectId: Long) -> List<Pair<Long, UserState>>,
        deleteFromDb: (List<Long>) -> Int,
        updateInDb: (List<Long>, UserState) -> Int,
    ): TaskCanceled {
        /**
         * When a project is canceled, only valid tasks in the project should be affected.
         */
        val (guestIds, memberOrLeaderIds) = getTaskIdsAndUserState(command.projectId)
            .filter { it.second.isValidState() }
            .partition { it.second == UserState.GUEST }
            .let { it.first.map { it.first } to it.second.map { it.first } }

        /**
         * Guest tasks are removed from Database
         */
        val affectedGuestCnt = deleteFromDb(guestIds)

        /**
         * The count of affected row should be equal to guests' count.
         */
        if (affectedGuestCnt != guestIds.size) {
            throw Exception()
        }

        /**
         * Official tasks turn into CANCELED state.
         */
        val affectedMemberOrLeaderCnt = updateInDb(memberOrLeaderIds, UserState.CANCELED)

        /**
         * The count of affected row should be equal to official members' count.
         */
        if (affectedMemberOrLeaderCnt != memberOrLeaderIds.size) {
            throw Exception()
        }

        return TaskCanceled(projectId = command.projectId)
    }

    fun complete(
        command: CompleteTask,
        getTaskIdsAndUserState: (projectId: Long) -> List<Pair<Long, UserState>>,
        deleteFromDb: (List<Long>) -> Int,
        updateInDb: (List<Long>, UserState) -> Int,
    ): TaskCompleted {
        /**
         * When a project is completed, only valid tasks in the project should be affected
         */
        val (guestIds, memberOrLeaderIds) = getTaskIdsAndUserState(command.projectId)
            .filter { it.second.isValidState() }
            .partition { it.second == UserState.GUEST }
            .let { it.first.map { it.first } to it.second.map { it.first } }

        /**
         * Guest tasks are removed from Database
         */
        val affectedGuestCnt = deleteFromDb(guestIds)

        /**
         * The count of affected row should be equal to guests' count.
         */
        if (affectedGuestCnt != guestIds.size) {
            throw Exception()
        }

        /**
         * Official tasks turn into CONTRIBUTED state.
         */
        val affectedMemberOrLeaderCnt = updateInDb(memberOrLeaderIds, UserState.CONTRIBUTED)

        /**
         * The count of affected row should be equal to official members' count.
         */
        if (affectedMemberOrLeaderCnt != memberOrLeaderIds.size) {
            throw Exception()
        }

        return TaskCompleted(projectId = command.projectId)
    }
}
