package waffle.guam.service.command

import waffle.guam.db.entity.Due
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectEntity

sealed class ProjectCommand

data class CreateProject(
    val title: String,
    val description: String,
    val thumbnail: String?,
    val frontHeadCnt: Int,
    val backHeadCnt: Int,
    val designHeadCnt: Int,
    val techStackIds: List<Pair<Long, Position>>,
    val due: Due,
    val myPosition: Position?
) : ProjectCommand() {
    fun toEntity(): ProjectEntity =
        ProjectEntity(
            title = title,
            description = description,
            thumbnail = thumbnail,
            frontHeadcount = frontHeadCnt,
            backHeadcount = backHeadCnt,
            designerHeadcount = designHeadCnt,
            recruiting = true,
            due = due
        )
}

data class UpdateProject(
    val title: String,
    val description: String,
    val thumbnail: String?,
    val frontHeadCnt: Int,
    val backHeadCnt: Int,
    val designHeadCnt: Int,
    val techStackIds: List<Pair<Long, Position>>,
    val due: Due
) : ProjectCommand() {
    fun toEntity(): ProjectEntity =
        ProjectEntity(
            title = title,
            description = description,
            thumbnail = thumbnail,
            frontHeadcount = frontHeadCnt,
            backHeadcount = backHeadCnt,
            designerHeadcount = designHeadCnt,
            recruiting = true,
            due = due
        )
}

data class JoinProject(
    val position: Position,
    val introduction: String
)
