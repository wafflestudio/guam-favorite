package waffle.guam.model

import waffle.guam.db.entity.State
import waffle.guam.db.entity.TaskMessage
import waffle.guam.db.entity.TaskStatus
import waffle.guam.db.entity.TaskView
import java.time.LocalDateTime

data class Task(
    val id: Long,
    val position: String,
    val taskMsg: String?,
    val projectId: Long,
    val user: User,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val state: State
) {
    companion object {
        fun of(e: TaskView, fetchMessage: Boolean = false) =
            Task(
                id = e.id,
                position = e.position.name,
                taskMsg = if (fetchMessage) getLatestMsg(e.tasks) else null,
                projectId = e.projectId,
                user = User.of(e.user),
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt,
                state = e.state
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
    val taskMsg: Set<TaskMessage>?,
    val projectId: Long,
    val user: User,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val state: State
) {
    companion object {
        fun of(e: TaskView, fetchMessage: Boolean = false) =
            TaskDetail(
                id = e.id,
                position = e.position.name,
                taskMsg = if (fetchMessage) e.tasks else null,
                projectId = e.projectId,
                user = User.of(e.user),
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt,
                state = e.state
            )
    }
}
