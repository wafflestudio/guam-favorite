package waffle.guam.project.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.db.entity.Due
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectEntity
import waffle.guam.db.entity.ProjectState
import waffle.guam.util.TypeCheck

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
        if (imageFiles != null) {
            TypeCheck.validImageFile(imageFiles!!)
        }
    }
    fun toEntity(): ProjectEntity =
        ProjectEntity(
            title = title ?: "default project title",
            description = description ?: "default project description",
            frontHeadcount = frontHeadCnt,
            backHeadcount = backHeadCnt,
            designerHeadcount = designHeadCnt,
            state = ProjectState.RECRUITING,
            due = due ?: Due.SIX
        )
}
