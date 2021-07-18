package waffle.guam.controller.request

import org.springframework.web.multipart.MultipartFile
import waffle.guam.db.entity.TaskStatus

data class ContentInput(
    val content: String
)

data class CreateFullInfoInput(
    val content: String?,
    val imageFiles: List<MultipartFile>?
)

data class CreateTaskMsgInput(
    val msg: String,
    val status: TaskStatus
)

data class UpdateTaskMsgInput(
    val msg: String?,
    val status: TaskStatus?
)
