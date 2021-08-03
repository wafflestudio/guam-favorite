package waffle.guam.user.model

import waffle.guam.db.entity.Position

data class UserProject(
    val projectId: Long,
    val projectTitle: String,
    val projectThumbnail: String?,
    val position: Position
)
