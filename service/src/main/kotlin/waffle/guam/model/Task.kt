package waffle.guam.model

import waffle.guam.db.entity.State
import waffle.guam.db.entity.TaskView
import java.time.LocalDateTime

data class Task(
    val id: Long,
    val position: String,
    val task: String,
    val projectId: Long,
    val user: User,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val state: State
) {
    companion object {
        fun of(e: TaskView) =
            Task(
                id = e.id,
                position = e.position.name,
                task = e.task,
                projectId = e.projectId,
                user = User.of(e.user),
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt,
                state = e.state
            )
    }
}
