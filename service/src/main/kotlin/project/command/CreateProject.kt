package waffle.guam.project.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.project.ProjectEntity
import waffle.guam.project.model.Due
import waffle.guam.project.model.ProjectState
import waffle.guam.task.model.Position
// import waffle.guam.util.TypeCheck

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
    val myPosition: Position?
) {
    init {
//        if (imageFiles != null) {
//            TypeCheck.validImageFile(imageFiles!!)
//        }
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
