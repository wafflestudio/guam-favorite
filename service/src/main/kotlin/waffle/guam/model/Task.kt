package waffle.guam.model

import waffle.guam.db.entity.TaskMessage
import waffle.guam.db.entity.TaskOverView
import waffle.guam.db.entity.TaskStatus
import waffle.guam.db.entity.TaskView
import waffle.guam.db.entity.UserState
import java.time.LocalDateTime

data class Task(
    val id: Long,
    val position: String,
    val taskMessages: String?,
    val projectId: Long,
    val user: User,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val userState: UserState
) {
    companion object {
        fun of(e: TaskView, fetchMessage: Boolean = false) =
            Task(
                id = e.id,
                position = e.position.name,
                taskMessages = if (fetchMessage) getLatestMsg(e.tasks) else null,
                projectId = e.projectId,
                user = User.of(e.user),
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt,
                userState = e.userState
            )

        fun of(e: TaskOverView) =
            Task(
                id = e.id,
                position = e.position.name,
                projectId = e.projectId,
                taskMessages = null,
                user = User.of(e.user),
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt,
                userState = e.userState
            )

        private fun getLatestMsg(messages: Set<TaskMessage>): String =
            messages.filter {
                it.status == TaskStatus.ONGOING
            }.maxByOrNull {
                it.modifiedAt
            }.let {
                it?.msg ?: "아직 아무 할 일도 작성하지 않으셨습니다."
            }
    }
}

data class TaskDetail(
    val id: Long,
    val position: String,
    val taskMessages: Set<TaskMessage>?,
    val projectId: Long,
    val user: User,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val userState: UserState
) {
    companion object {
        fun of(e: TaskView, fetchMessage: Boolean = false) =
            TaskDetail(
                id = e.id,
                position = e.position.name,
                taskMessages =
                if (fetchMessage) e.tasks.toList()
                    .filter{
                        it.status != TaskStatus.DELETED
                    }
                    .sortedWith(
                        compareByDescending<TaskMessage> { it.status }
                            .thenByDescending { it.modifiedAt }
                    )
                    .toSet()
                else null,
                projectId = e.projectId,
                user = User.of(e.user),
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt,
                userState = e.userState
            )
    }
}

data class TaskOverview(
    val id: Long,
    val user: Map<String, Any>,
    val userState: UserState
) {
    companion object {
        fun of(e: TaskView) =
            TaskOverview(
                id = e.id,
                user = e.user.let {
                    val map = mutableMapOf<String, Any>()
                    map["id"] = it.id
                    map
                },
                userState = e.userState
            )
    }
}
