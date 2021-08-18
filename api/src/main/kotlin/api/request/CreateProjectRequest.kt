package waffle.guam.api.request

import org.springframework.web.multipart.MultipartFile
import waffle.guam.project.command.CreateProject
import waffle.guam.project.model.Due
import waffle.guam.task.model.Position

data class CreateProjectRequest(
    val title: String?,
    val description: String?,
    var imageFiles: MultipartFile?,
    val frontHeadCnt: Int,
    val backHeadCnt: Int,
    val designHeadCnt: Int,
    val frontStackId: Long?,
    val backStackId: Long?,
    val designStackId: Long?,
    val due: Due?,
    val myPosition: Position?
) {
    fun toCommand() = CreateProject(
        title = title,
        description = description,
        imageFiles = imageFiles,
        frontHeadCnt = frontHeadCnt,
        backHeadCnt = backHeadCnt,
        designHeadCnt = designHeadCnt,
        frontStackId = frontStackId,
        backStackId = backStackId,
        designStackId = designStackId,
        due = due,
        myPosition = myPosition
    )
}
