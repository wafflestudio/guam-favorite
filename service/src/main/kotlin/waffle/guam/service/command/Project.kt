package waffle.guam.service.command

import waffle.guam.db.entity.ProjectEntity

sealed class ProjectCommand

data class CreateProject(
    val title: String,
    val description: String,
    val thumbnail: String,
    val frontLeftCnt: Int,
    val backLeftCnt: Int,
    val designLeftCnt: Int,
    val techStackIds: List<Long>,
) : ProjectCommand() {
    fun toEntity(): ProjectEntity =
        ProjectEntity(
            title = title,
            description = description,
            thumbnail = thumbnail,
            frontHeadcount = frontLeftCnt,
            backHeadcount = backLeftCnt,
            designerHeadcount = designLeftCnt,
            isRecruiting = true
        )
}
