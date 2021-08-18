package waffle.guam.service.command

import org.springframework.web.multipart.MultipartFile
import waffle.guam.db.entity.Due
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectEntity
import waffle.guam.db.entity.ProjectState
import waffle.guam.exception.InvalidRequestException
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

        when (myPosition) {
            Position.DESIGNER -> if (designHeadCnt < 1) throw InvalidRequestException("나의 포지션 $myPosition 로 참여할 수 있는 정원이 없어요.")
            Position.FRONTEND -> if (frontHeadCnt < 1) throw InvalidRequestException("나의 포지션 $myPosition 로 참여할 수 있는 정원이 없어요.")
            Position.BACKEND -> if (backHeadCnt < 1) throw InvalidRequestException("나의 포지션 $myPosition 로 참여할 수 있는 정원이 없어요.")
            Position.WHATEVER -> throw InvalidRequestException("WHATEVER 는 아직 설정할 수 없는 포지션입니다.")
        }

        if (imageFiles != null) {
            TypeCheck.validImageFile(imageFiles!!)
        }
        if (frontHeadCnt + backHeadCnt + designHeadCnt == 0)
            throw InvalidRequestException("적어도 한 명의 구성원이 필요합니다.")
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
