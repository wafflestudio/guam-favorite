package waffle.guam.service.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.db.entity.Due
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectEntity
import waffle.guam.db.entity.ProjectState
import waffle.guam.util.TypeCheck

sealed class ProjectCommand

data class CreateProject(
    val title: String?,
    val description: String?,
    var imageFiles: MultipartFile?,
    val frontHeadCnt: Int,
    val backHeadCnt: Int,
    val designHeadCnt: Int,
    // TODO rename techStackIds
    val frontStackId: Long?,
    val backStackId: Long?,
    val designStackId: Long?,
    val due: Due?,
    val myPosition: Position?
) : ProjectCommand() {
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
) : ProjectCommand() {
    init {
        imageFiles?.let {
            TypeCheck.validImageFile(it)
        }
    }
}

data class JoinProject(
    val position: Position,
    val introduction: String
)

// legacy code
data class StackInfo(
    val stackId: Long,
    val position: Position
) {
    companion object {
        fun of(stackInfo: String): StackInfo {
            val ls = stackInfo.split(",")
            if (ls.size != 2) throw IllegalArgumentException("stackInfo format error")
            return StackInfo(
                ls[0].toLong(),
                Position.valueOf(ls[1].trim())
            )
        }
    }
}
