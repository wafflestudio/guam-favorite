package waffle.guam.project.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.InvalidRequestException
import waffle.guam.project.ProjectEntity
import waffle.guam.project.model.Due
import waffle.guam.project.model.ProjectState
import waffle.guam.task.model.Position

data class CreateProject(
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
    val myPosition: Position?,
) {
    init {
        if (frontHeadCnt < 0 || backHeadCnt <= 0 || designHeadCnt <= 0) {
            throw InvalidRequestException("TODO")
        }

        if (myPosition == null || myPosition == Position.WHATEVER)
            throw InvalidRequestException("포지션을 입력해주세요")
    }

    fun toEntity(): ProjectEntity =
        ProjectEntity(
            title = title ?: "default project title",
            description = description ?: "default project description",
            frontHeadcount = frontHeadCnt,
            backHeadcount = backHeadCnt,
            designerHeadcount = designHeadCnt,
            state = ProjectState.RECRUITING.name,
            due = (due ?: Due.SIX).name
        )
}
