package waffle.guam.api.response

import com.fasterxml.jackson.annotation.JsonFormat
import waffle.guam.task.model.Position
import waffle.guam.task.model.Task
import waffle.guam.task.model.UserState
import java.time.Instant

data class TaskResponse(
    val id: Long,
    val position: Position,
    val projectId: Long,
    val user: UserResponse?,
    val userState: UserState?,
    val taskMessages: List<TaskMessageResponse>? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "Asia/Seoul")
    val createdAt: Instant,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "Asia/Seoul")
    val modifiedAt: Instant,
) {
    companion object {
        fun of(d: Task) = TaskResponse(
            id = d.id,
            position = d.position,
            projectId = d.projectId,
            user = d.user?.let { UserResponse.of(it) },
            userState = d.userState,
            taskMessages = d.taskMessages?.map { TaskMessageResponse.of(it) },
            createdAt = d.createdAt,
            modifiedAt = d.modifiedAt
        )
    }
}
