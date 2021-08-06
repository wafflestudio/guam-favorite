package waffle.guam.project.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.db.entity.Due
import waffle.guam.util.TypeCheck

data class UpdateProject(
    val title: String?,
    val description: String?,
    var imageFiles: MultipartFile?,
    val frontHeadCnt: Int,
    val backHeadCnt: Int,
    val designHeadCnt: Int,
    val frontStackId: Long?,
    val backStackId: Long?,
    val designStackId: Long?,
    val due: Due?
) {
    init {
        imageFiles?.let {
            TypeCheck.validImageFile(it)
        }
    }
}
