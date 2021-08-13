package waffle.guam.project.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.project.model.Due

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
//        imageFiles?.let {
//            TypeCheck.validImageFile(it)
//        }
    }
}
