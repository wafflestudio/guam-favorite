package waffle.guam.api.response

import com.fasterxml.jackson.annotation.JsonFormat
import waffle.guam.taskmessage.model.TaskMessage
import waffle.guam.taskmessage.model.TaskStatus
import java.time.Instant

data class TaskMessageResponse(
    val id: Long,
    val msg: String,
    var status: TaskStatus,
    val taskId: Long,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "Asia/Seoul")
    val createdAt: Instant,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "Asia/Seoul")
    val modifiedAt: Instant,
) {
    companion object {
        fun of(d: TaskMessage) = TaskMessageResponse(
            id = d.id,
            msg = d.msg,
            status = d.status,
            taskId = d.taskId,
            createdAt = d.createdAt,
            modifiedAt = d.modifiedAt
        )
    }
}
